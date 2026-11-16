package com.griot.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.language.SongLanguageDetector
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongSection
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SongEditorRepository
import com.griot.core.data.song.SongRepository
import com.griot.core.phonetics.paste.PastedBlock
import com.griot.core.phonetics.paste.splitPastedText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The editor of the song [songId]: every change is saved at once. */
// One function per change the editor makes.
@Suppress("TooManyFunctions")
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = EditorViewModel.Factory::class)
internal class EditorViewModel @AssistedInject constructor(
    @Assisted private val songId: String,
    private val editor: SongEditorRepository,
    private val songs: SongRepository,
    private val preferences: UserPreferencesRepository,
    private val detector: SongLanguageDetector,
) : ViewModel() {
    private val focus = MutableStateFlow<VerseFocus?>(null)
    private val pasteUndo = MutableStateFlow<PasteUndo?>(null)

    // The whole song, detected again 1 s after the last change: a few English words do not switch a French song.
    private val detectedLanguage = editor.observeDocument(songId)
        .map { document -> document?.sections.orEmpty().filter { it.repeatOf == null }.flatMap { it.verses } }
        .map { verses -> verses.joinToString(separator = "\n") { it.text } }
        .distinctUntilChanged()
        .debounce(DETECTION_DELAY_MILLIS)
        .mapLatest { lyrics -> detector.detect(lyrics) }
        .onStart { emit(null) }

    val uiState: StateFlow<EditorUiState> = editor.observeDocument(songId)
        .map { document ->
            if (document == null) {
                EditorUiState(loading = false, missing = true)
            } else {
                val labels = sectionLabels(document.sections)
                EditorUiState(
                    loading = false,
                    title = document.title,
                    language = document.language,
                    sections = document.sections.mapIndexed { index, section ->
                        EditorSection(
                            section = section,
                            label = labels[index],
                            first = index == 0,
                            last = index == document.sections.lastIndex,
                        )
                    },
                )
            }
        }
        .combine(focus) { state, focus -> state.copy(focus = focus) }
        .combine(preferences.editorFontSize) { state, size -> state.copy(fontSize = size) }
        .combine(pasteUndo) { state, undo -> state.copy(pasteUndo = undo) }
        .combine(detectedLanguage) { state, detected ->
            state.copy(
                detectedLanguage = detected,
                writingLanguage = if (state.language == SongLanguage.AUTOMATIC) detected else state.language,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), EditorUiState())

    fun onLanguageSelected(language: SongLanguage) {
        viewModelScope.launch { editor.setLanguage(songId = songId, language = language) }
    }

    fun onTitleChange(title: String) {
        viewModelScope.launch { songs.renameSong(id = songId, title = title.singleLine()) }
    }

    fun onAddSection(kind: SectionKind) {
        viewModelScope.launch { editor.addSection(songId = songId, kind = kind) }
    }

    fun onRepeatSection(sectionId: String) {
        viewModelScope.launch { editor.repeatSection(sectionId) }
    }

    fun onRenameSection(sectionId: String, name: String) {
        viewModelScope.launch { editor.renameSection(sectionId = sectionId, name = name) }
    }

    fun onMoveSection(sectionId: String, offset: Int) {
        viewModelScope.launch { editor.moveSection(sectionId = sectionId, offset = offset) }
    }

    fun onDeleteSection(sectionId: String) {
        viewModelScope.launch { editor.deleteSection(sectionId) }
    }

    /** Saves the verse as typed; a line break is not part of a verse (GRI-15 turns it into a new verse). */
    fun onVerseChange(verseId: String, text: String) {
        viewModelScope.launch { editor.updateVerse(verseId = verseId, text = text.singleLine()) }
    }

    /**
     * The verse [verseId] was typed with line breaks, into [lines]; [cursor] is where the cursor was in the last one.
     * Enter on an empty last verse of a section closes the section and starts a new one; otherwise each line after
     * the first becomes a new verse, and the last one takes the focus.
     */
    fun onVerseLines(verseId: String, lines: List<String>, cursor: Int) {
        val section = sectionOf(verseId) ?: return
        val blocks = splitPastedText(lines.joinToString(separator = "\n"))
        if (blocks.size > 1) {
            paste(verseId = verseId, lines = lines, blocks = blocks)
            return
        }
        val closesSection = lines.size == 2 &&
            lines.all { it.isEmpty() } &&
            section.verses.size > 1 &&
            section.verses.last().id == verseId
        viewModelScope.launch {
            focus.value = if (closesSection) {
                VerseFocus(verseId = editor.closeSection(verseId), text = "", cursor = 0)
            } else {
                val added = editor.splitVerse(verseId = verseId, lines = lines)
                VerseFocus(
                    verseId = added.last(),
                    text = lines.last(),
                    cursor = cursor.coerceIn(0, lines.last().length),
                )
            }
        }
    }

    // A text that holds several sections: they are created, and the undo bar can turn them back into one section.
    private fun paste(verseId: String, lines: List<String>, blocks: List<PastedBlock>) {
        viewModelScope.launch {
            val paste = editor.pasteSections(verseId = verseId, sections = blocks.toNewSections())
            pasteUndo.value = PasteUndo(
                verseId = verseId,
                lines = lines.filter { it.isNotBlank() }.ifEmpty { listOf("") },
                paste = paste,
                sectionCount = blocks.size,
            )
        }
    }

    /** Turns the last paste back into one section, one verse per line. */
    fun onUndoPaste() {
        val undo = pasteUndo.value ?: return
        pasteUndo.value = null
        viewModelScope.launch { editor.undoPaste(paste = undo.paste, verseId = undo.verseId, lines = undo.lines) }
    }

    fun onPasteUndoDismissed() {
        pasteUndo.value = null
    }

    /** Backspace at the start of the verse [verseId], holding [text]: it joins the previous verse of its section. */
    fun onVerseBackspaceAtStart(verseId: String, text: String) {
        val verses = sectionOf(verseId)?.verses ?: return
        val previous = verses.getOrNull(verses.indexOfFirst { it.id == verseId } - 1) ?: return
        viewModelScope.launch {
            editor.mergeVerses(previousId = previous.id, verseId = verseId)
            focus.value = VerseFocus(verseId = previous.id, text = previous.text + text, cursor = previous.text.length)
        }
    }

    /** Shows the lyrics at [size] sp in every song, kept between [MIN_FONT_SIZE] and [MAX_FONT_SIZE]. */
    fun onFontSizeChange(size: Int) {
        viewModelScope.launch { preferences.setEditorFontSize(size.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)) }
    }

    /** The field asked to take the focus has it. */
    fun onFocusHandled() {
        focus.value = null
    }

    // The first section showing the verse: the source of a repeat, or the repeat, hold the same verses.
    private fun sectionOf(verseId: String): SongSection? =
        uiState.value.sections.map { it.section }.firstOrNull { section -> section.verses.any { it.id == verseId } }

    private fun String.singleLine() = replace('\n', ' ')

    @AssistedFactory
    interface Factory {
        fun create(songId: String): EditorViewModel
    }

    companion object {
        /** Smallest size of the lyrics, in sp. */
        const val MIN_FONT_SIZE = 14

        /** Largest size of the lyrics, in sp. */
        const val MAX_FONT_SIZE = 28

        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val DETECTION_DELAY_MILLIS = 1_000L
    }
}
