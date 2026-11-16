package com.griot.feature.onboarding.name

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
internal class NameViewModel @Inject constructor(private val preferences: UserPreferencesRepository) : ViewModel() {
    private val state = MutableStateFlow(NameUiState())
    val uiState: StateFlow<NameUiState> = state.asStateFlow()

    // Whether « Continuer » was tapped once: from then on, an empty name is pointed out.
    private var attempted = false

    init {
        // The name given before, when the user comes back to this step.
        viewModelScope.launch {
            val saved = preferences.authorName.first().orEmpty()
            state.update { if (it.name.isEmpty()) it.copy(name = saved) else it }
        }
    }

    fun onNameChange(name: String) {
        state.update { it.copy(name = name, emptyNameShown = attempted && name.isBlank()) }
    }

    /** Saves the name, without the spaces around it, then calls [onSaved]; points out an empty name instead. */
    fun onContinue(onSaved: () -> Unit) {
        attempted = true
        val name = state.value.name.trim()
        if (name.isEmpty()) {
            state.update { it.copy(emptyNameShown = true) }
            return
        }
        viewModelScope.launch {
            preferences.setAuthorName(name)
            onSaved()
        }
    }
}
