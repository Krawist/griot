package com.griot.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.export.MarkdownExporter
import com.griot.core.data.language.AppLanguageRepository
import com.griot.core.data.model.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val appLanguage: AppLanguageRepository,
    private val exporter: MarkdownExporter,
) : ViewModel() {
    private val state = MutableStateFlow(SettingsUiState(language = appLanguage.current()))
    val uiState: StateFlow<SettingsUiState> = state.asStateFlow()

    /** Applies [language] to the interface at once. */
    fun onLanguageSelected(language: AppLanguage) {
        appLanguage.set(language)
        state.update { it.copy(language = language) }
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
