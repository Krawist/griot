package com.griot.feature.onboarding.importing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.importing.DocumentReader
import com.griot.core.data.importing.TextImporter
import com.griot.core.data.model.ImportResult
import com.griot.core.data.model.ImportedText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
internal class ImportViewModel @Inject constructor(
    private val reader: DocumentReader,
    private val importer: TextImporter,
) : ViewModel() {
    private val state = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = state.asStateFlow()

    /** Imports the files picked, one song per file. */
    fun onFilesPicked(uris: List<String>) {
        if (uris.isEmpty()) return
        run { importer.import(reader.read(uris)) }
    }

    /** Imports a pasted [text] as one song, not named yet. */
    fun onPaste(text: String) {
        if (text.isBlank()) return
        run { importer.import(listOf(ImportedText(name = "", text = text))) }
    }

    // Each import adds to what the screen shows.
    private fun run(import: suspend () -> ImportResult) {
        state.update { it.copy(importing = true) }
        viewModelScope.launch {
            val result = import()
            state.update { current ->
                val before = current.result
                current.copy(
                    importing = false,
                    result = ImportResult(
                        songs = (before?.songs ?: 0) + result.songs,
                        sections = (before?.sections ?: 0) + result.sections,
                        failed = before?.failed.orEmpty() + result.failed,
                    ),
                )
            }
        }
    }
}
