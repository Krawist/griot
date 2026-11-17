package com.griot.feature.editor.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.ai.AiAnswer
import com.griot.core.ai.AiAssistant
import com.griot.core.ai.AiLocation
import com.griot.core.ai.CheckedSuggestion
import com.griot.core.ai.VerseRequest
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.song.SongEditorRepository
import com.griot.core.data.song.sectionLabels
import com.griot.core.phonetics.PhoneticEngines
import com.griot.core.phonetics.PhoneticLanguage
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The AI panel of the verse [verseId] of the song [songId], read in [language]: its rhymes from the dictionary, and
 * the versions and remarks of a model, asked when their tab opens and again on « Autres idées ».
 */
@HiltViewModel(assistedFactory = AiPanelViewModel.Factory::class)
internal class AiPanelViewModel @AssistedInject constructor(
    @Assisted("songId") songId: String,
    @Assisted("verseId") private val verseId: String,
    @Assisted language: SongLanguage,
    editor: SongEditorRepository,
    engines: PhoneticEngines,
    private val assistant: AiAssistant,
) : ViewModel() {
    private val phoneticLanguage =
        if (language == SongLanguage.ENGLISH) PhoneticLanguage.ENGLISH else PhoneticLanguage.FRENCH
    private val engine = engines.of(phoneticLanguage)
    private val tab = MutableStateFlow(AiTab.RHYMES)
    private val rephrase = MutableStateFlow(AiContent<CheckedSuggestion>())
    private val critique = MutableStateFlow(AiContent<String>())
    private val location = MutableStateFlow<AiLocation?>(null)

    // The rhymes read the dictionary: off the main thread, and only when the verse's text or the song's words change.
    private val context: StateFlow<VerseContext?> = editor.observeDocument(songId)
        .map { document -> document?.let(::contextOf) }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState: StateFlow<AiPanelUiState> = combine(
        context,
        tab,
        rephrase,
        critique,
        location,
    ) { context, tab, rephrase, critique, location ->
        AiPanelUiState(
            loading = context == null,
            verse = context?.request?.verse.orEmpty(),
            section = context?.section,
            number = context?.number ?: 0,
            syllables = context?.request?.targetSyllables ?: 0,
            ending = context?.ending.orEmpty(),
            tab = tab,
            rhymes = context?.rhymes.orEmpty(),
            rephrase = rephrase,
            critique = critique,
            location = location,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AiPanelUiState())

    init {
        viewModelScope.launch { location.value = assistant.location() }
    }

    /** Shows [selected]; a model tab asks the model the first time it opens. */
    fun onTabSelected(selected: AiTab) {
        tab.value = selected
        val content = if (selected == AiTab.REPHRASE) rephrase.value else critique.value
        if (selected != AiTab.RHYMES && !content.answered && !content.loading) onMoreIdeas()
    }

    /** Asks the model of the tab shown for new suggestions or remarks. */
    fun onMoreIdeas() {
        viewModelScope.launch {
            val request = context.filterNotNull().first().request
            when (tab.value) {
                AiTab.RHYMES -> Unit
                AiTab.REPHRASE -> ask(rephrase) { assistant.rephrase(request) }
                AiTab.CRITIQUE -> ask(critique) { assistant.critique(request) }
            }
        }
    }

    // A failure of the model shows a retry: the text is never touched.
    @Suppress("TooGenericExceptionCaught") // Any failure of a model, on the phone or online.
    private suspend fun <T> ask(content: MutableStateFlow<AiContent<T>>, call: suspend () -> AiAnswer<T>?) {
        content.update { it.copy(loading = true, failed = false) }
        try {
            val answer = call()
            location.value = answer?.location
            content.value = AiContent(items = answer?.items.orEmpty(), answered = true, unavailable = answer == null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "The model did not answer", e)
            content.update { it.copy(loading = false, failed = true) }
        }
    }

    // The verse in its first section (a repeat shows the same verse), with its neighbours and its rhymes.
    private fun contextOf(document: SongDocument): VerseContext? {
        val labels = sectionLabels(document.sections)
        val index = document.sections.indexOfFirst { section -> section.verses.any { it.id == verseId } }
        if (index < 0) return null
        val verses = document.sections[index].verses
        val position = verses.indexOfFirst { it.id == verseId }
        val text = verses[position].text
        val label = labels[index]
        val used = document.sections.filter { it.repeatOf == null }
            .flatMap { section -> section.verses.flatMap { words(it.text) } }
            .toSet()
        return VerseContext(
            request = VerseRequest(
                language = phoneticLanguage,
                section = listOfNotNull(label.name ?: label.kind.name.lowercase(), label.number).joinToString(" "),
                verse = text,
                before = verses.take(position).map { it.text },
                after = verses.drop(position + 1).map { it.text },
                targetSyllables = engine.countSyllables(text).syllables,
            ),
            section = label,
            number = position + 1,
            ending = engine.rhyme(text)?.let { text.substring(it.start, it.end) }.orEmpty(),
            rhymes = engine.rhymingWords(text).map {
                RhymeChip(word = it.word, syllables = it.syllables, used = it.word in used)
            },
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("songId") songId: String,
            @Assisted("verseId") verseId: String,
            language: SongLanguage,
        ): AiPanelViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val TAG = "AiPanel"
    }
}

// The words of a verse in lower case, apostrophes splitting them (« l'amour » → « l », « amour »).
private fun words(text: String): List<String> = text.lowercase().split(NotWord).filter { it.isNotEmpty() }

private val NotWord = Regex("""[^\p{L}-]+""")
