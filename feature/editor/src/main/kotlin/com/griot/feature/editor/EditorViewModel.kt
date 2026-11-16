package com.griot.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.model.SectionKind
import com.griot.core.data.song.SongEditorRepository
import com.griot.core.data.song.SongRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The editor of the song [songId]: every change is saved at once. */
@HiltViewModel(assistedFactory = EditorViewModel.Factory::class)
internal class EditorViewModel @AssistedInject constructor(
    @Assisted private val songId: String,
    private val editor: SongEditorRepository,
    private val songs: SongRepository,
) : ViewModel() {
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

    private fun String.singleLine() = replace('\n', ' ')

    @AssistedFactory
    interface Factory {
        fun create(songId: String): EditorViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
