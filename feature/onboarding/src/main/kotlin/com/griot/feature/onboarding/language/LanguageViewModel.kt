package com.griot.feature.onboarding.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.language.AppLanguageRepository
import com.griot.core.data.model.AppLanguage
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
internal class LanguageViewModel @Inject constructor(
    private val appLanguage: AppLanguageRepository,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {
    private val state = MutableStateFlow(LanguageUiState(selected = preselected()))
    val uiState: StateFlow<LanguageUiState> = state.asStateFlow()

    fun onLanguageSelected(language: AppLanguage) {
        state.update { it.copy(selected = language) }
    }

    /**
     * Saves the language picked as the writing language of new songs, calls [onSaved], then applies it to the
     * interface: Android redraws the screens, so the next step must already be the current one.
     */
    fun onContinue(onSaved: () -> Unit) {
        val selected = state.value.selected
        viewModelScope.launch {
            preferences.setDefaultSongLanguage(
                if (selected == AppLanguage.ENGLISH) SongLanguage.ENGLISH else SongLanguage.FRENCH,
            )
            onSaved()
            appLanguage.set(selected)
        }
    }

    // The language picked before, else French when the phone is in French and English otherwise.
    private fun preselected(): AppLanguage = when (appLanguage.current()) {
        AppLanguage.FRENCH -> AppLanguage.FRENCH
        AppLanguage.ENGLISH -> AppLanguage.ENGLISH
        AppLanguage.SYSTEM ->
            if (Locale.getDefault().language == Locale.FRENCH.language) AppLanguage.FRENCH else AppLanguage.ENGLISH
    }
}
