package com.griot.feature.settings

import androidx.lifecycle.ViewModel
import com.griot.core.data.language.AppLanguageRepository
import com.griot.core.data.model.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
internal class SettingsViewModel @Inject constructor(private val appLanguage: AppLanguageRepository) : ViewModel() {
    private val state = MutableStateFlow(SettingsUiState(language = appLanguage.current()))
    val uiState: StateFlow<SettingsUiState> = state.asStateFlow()

    /** Applies [language] to the interface at once. */
    fun onLanguageSelected(language: AppLanguage) {
        appLanguage.set(language)
        state.update { it.copy(language = language) }
    }
}
