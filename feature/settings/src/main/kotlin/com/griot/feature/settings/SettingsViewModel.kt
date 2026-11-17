package com.griot.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.ai.AiAssistant
import com.griot.core.ai.AiLocation
import com.griot.core.ai.online.ApiKeyRepository
import com.griot.core.data.audio.ExternalMusic
import com.griot.core.data.export.MarkdownExporter
import com.griot.core.data.language.AppLanguageRepository
import com.griot.core.data.model.AppLanguage
import com.griot.core.data.model.PreferredAi
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions") // One action per setting.
@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val appLanguage: AppLanguageRepository,
    private val preferences: UserPreferencesRepository,
    private val exporter: MarkdownExporter,
    private val assistant: AiAssistant,
    private val externalMusic: ExternalMusic,
    private val apiKeys: ApiKeyRepository,
) : ViewModel() {
    private val state = MutableStateFlow(SettingsUiState(language = appLanguage.current()))
    val uiState: StateFlow<SettingsUiState> = state.asStateFlow()

    init {
        combine(preferences.authorName, preferences.editorFontSize, preferences.onDeviceAi) { name, size, onDevice ->
            state.update { it.copy(authorName = name.orEmpty(), fontSize = size, onDeviceAi = onDevice) }
        }.launchIn(viewModelScope)
        combine(preferences.onlineAi, preferences.preferredAi, apiKeys.isSet) { online, preferred, keySet ->
            state.update { it.copy(onlineAi = online, preferredAi = preferred, apiKeySet = keySet) }
        }.launchIn(viewModelScope)
        viewModelScope.launch {
            val status = assistant.status(AiLocation.DEVICE)
            state.update { it.copy(onDeviceStatus = status) }
        }
    }

    /** Reads again whether the notification access is given: the author may come back from the phone's settings. */
    fun onResume() {
        state.update { it.copy(externalMusic = externalMusic.hasAccess()) }
    }

    /** Saves [name] without the spaces around it; an empty name keeps the one before. */
    fun onNameChange(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { preferences.setAuthorName(trimmed) }
    }

    /**
     * Applies [language] to the interface; French or English also becomes the language of new songs, as in the
     * onboarding. The preference is saved first, as Android redraws the screens once the language applies.
     */
    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            when (language) {
                AppLanguage.FRENCH -> preferences.setDefaultSongLanguage(SongLanguage.FRENCH)
                AppLanguage.ENGLISH -> preferences.setDefaultSongLanguage(SongLanguage.ENGLISH)
                AppLanguage.SYSTEM -> Unit
            }
            state.update { it.copy(language = language) }
            appLanguage.set(language)
        }
    }

    /** Allows or forbids the AI help to use the model on the phone. */
    fun onOnDeviceAiChange(allowed: Boolean) {
        viewModelScope.launch { preferences.setOnDeviceAi(allowed) }
    }

    /** Allows or forbids the AI help to use the online model. */
    fun onOnlineAiChange(allowed: Boolean) {
        viewModelScope.launch { preferences.setOnlineAi(allowed) }
    }

    /** Saves the model the AI help asks first. */
    fun onPreferredAiChange(preferred: PreferredAi) {
        viewModelScope.launch { preferences.setPreferredAi(preferred) }
    }

    /** Asks Anthropic about the pasted [key], and saves it once accepted. */
    fun onApiKeySave(key: String) {
        if (state.value.checkingKey) return
        state.update { it.copy(checkingKey = true, keyCheck = null) }
        viewModelScope.launch {
            val check = apiKeys.save(key)
            state.update { it.copy(checkingKey = false, keyCheck = check) }
        }
    }

    /** Forgets the key of the online model. */
    fun onApiKeyClear() {
        viewModelScope.launch { apiKeys.clear() }
    }

    /** The key sheet is closed: what Anthropic said is forgotten. */
    fun onApiKeyClosed() {
        state.update { it.copy(keyCheck = null) }
    }

    /** Exports every song as Markdown in a zip, then shows what to do with the file. */
    fun onExportAll() {
        viewModelScope.launch {
            val file = exporter.exportAll()
            state.update { it.copy(export = file) }
        }
    }

    /** Saves the exported file in the document [uri] the user picked. */
    fun onSaveExport(uri: String) {
        val file = state.value.export ?: return
        viewModelScope.launch { exporter.save(file = file, uri = uri) }
    }

    fun onExportClosed() {
        state.update { it.copy(export = null) }
    }
}
