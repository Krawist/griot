package com.griot.feature.editor.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.audio.ExternalMusic
import com.griot.core.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The music of another app in the editor's player (GRI-53), and whether to explain, the first time it shows, what
 * the notification access adds.
 */
@HiltViewModel
internal class ExternalMusicViewModel @Inject constructor(
    private val music: ExternalMusic,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {
    val uiState: StateFlow<ExternalMusicUiState> =
        combine(music.observe(), preferences.externalMusicExplained) { state, explained ->
            ExternalMusicUiState(music = state, explain = state.active && !state.controlled && !explained)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ExternalMusicUiState())

    fun onPlayToggle() {
        music.playPause()
    }

    /** « Reculer de 15 secondes », with the access only. */
    fun onRewind() {
        music.rewind(REWIND_MILLIS)
    }

    /** The explanation was shown, accepted or put off: it does not come back. */
    fun onExplained() {
        viewModelScope.launch { preferences.setExternalMusicExplained() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val REWIND_MILLIS = 15_000L
    }
}
