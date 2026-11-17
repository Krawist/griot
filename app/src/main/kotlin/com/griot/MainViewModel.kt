package com.griot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.ai.AiAssistant
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SongRepository
import com.griot.core.data.song.VersionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    songs: SongRepository,
    versions: VersionRepository,
    assistant: AiAssistant,
) : ViewModel() {
    val uiState: StateFlow<MainUiState> = preferences.onboardingCompleted
        .map { MainUiState.Ready(onboardingCompleted = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MainUiState.Loading)

    init {
        // Once per launch: the songs in the trash for more than 30 days are removed for good, and the old versions
        // are thinned out to one per day.
        viewModelScope.launch {
            songs.purgeTrash()
            versions.thin()
        }
        // The AI models are checked at launch: a phone that can have Gemini Nano starts downloading it.
        viewModelScope.launch { assistant.availability() }
    }

    fun onOnboardingCompleted() {
        viewModelScope.launch { preferences.setOnboardingCompleted() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
