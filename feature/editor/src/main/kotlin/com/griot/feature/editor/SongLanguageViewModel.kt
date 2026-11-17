package com.griot.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.language.SongLanguageDetector
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.song.SongEditorRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The writing language of the song [songId], from the editor or from a long press on the song: a language set by hand,
 * or the automatic mode, which detects the language of the lyrics at once.
 */
@HiltViewModel(assistedFactory = SongLanguageViewModel.Factory::class)
internal class SongLanguageViewModel @AssistedInject constructor(
    @Assisted private val songId: String,
    private val editor: SongEditorRepository,
    private val detector: SongLanguageDetector,
) : ViewModel() {
    val uiState: StateFlow<SongLanguageUiState> = editor.observeDocument(songId)
        .map { document ->
            SongLanguageUiState(
                choice = when {
                    document == null -> LanguageChoice.FRENCH
                    document.languageDetected -> LanguageChoice.AUTOMATIC
                    document.language == SongLanguage.ENGLISH -> LanguageChoice.ENGLISH
                    else -> LanguageChoice.FRENCH
                },
                language = document?.language ?: SongLanguage.FRENCH,
                loading = false,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SongLanguageUiState())

    /** Sets the language [choice]; the automatic mode keeps the language when the lyrics are too short to tell. */
    fun onApply(choice: LanguageChoice) {
        viewModelScope.launch {
            when (choice) {
                LanguageChoice.FRENCH -> editor.setLanguage(songId, language = SongLanguage.FRENCH, detected = false)
                LanguageChoice.ENGLISH -> editor.setLanguage(songId, language = SongLanguage.ENGLISH, detected = false)
                LanguageChoice.AUTOMATIC -> {
                    val document = editor.observeDocument(songId).first() ?: return@launch
                    val language = detector.detect(document.lyrics()) ?: document.language
                    editor.setLanguage(songId = songId, language = language, detected = true)
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(songId: String): SongLanguageViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
