package com.griot

/** What the root of the app needs before showing its first screen. */
internal sealed interface MainUiState {
    data object Loading : MainUiState

    data class Ready(val onboardingCompleted: Boolean) : MainUiState
}
