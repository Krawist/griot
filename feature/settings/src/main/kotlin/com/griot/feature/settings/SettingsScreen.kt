package com.griot.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.AppLanguage
import com.griot.core.design.component.PlaceholderAction
import com.griot.core.design.component.PlaceholderScreen

/** Settings, a placeholder until GRI-29: for now, the language of the interface only. */
@Composable
internal fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlaceholderScreen(
        title = stringResource(R.string.settings_title),
        subtitle = stringResource(R.string.settings_language_current, stringResource(uiState.language.labelRes())),
        actions = AppLanguage.entries.map { language ->
            PlaceholderAction(stringResource(language.labelRes())) { viewModel.onLanguageSelected(language) }
        },
    )
}

private fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.SYSTEM -> R.string.settings_language_system
    AppLanguage.FRENCH -> R.string.settings_language_french
    AppLanguage.ENGLISH -> R.string.settings_language_english
}
