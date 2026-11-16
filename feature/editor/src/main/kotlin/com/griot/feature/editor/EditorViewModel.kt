package com.griot.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongSection
import com.griot.core.data.song.SongEditorRepository
import com.griot.core.data.song.SongRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The editor of the song [songId]: every change is saved at once. */
// One function per change the editor makes.
@Suppress("TooManyFunctions")
@HiltViewModel(assistedFactory = EditorViewModel.Factory::class)
internal class EditorViewModel @AssistedInject constructor(
    @Assisted private val songId: String,
    private val editor: SongEditorRepository,
    private val songs: SongRepository,
) : ViewModel() {
    private val focus = MutableStateFlow<VerseFocus?>(null)

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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), EditorUiState())

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

    /** Backspace at the start of the verse [verseId], holding [text]: it joins the previous verse of its section. */
    fun onVerseBackspaceAtStart(verseId: String, text: String) {
        val verses = sectionOf(verseId)?.verses ?: return
        val previous = verses.getOrNull(verses.indexOfFirst { it.id == verseId } - 1) ?: return
        viewModelScope.launch {
            editor.mergeVerses(previousId = previous.id, verseId = verseId)
            focus.value = VerseFocus(verseId = previous.id, text = previous.text + text, cursor = previous.text.length)
        }
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

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
