package com.griot.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.AppLanguage
import com.griot.core.design.component.ChoiceOption
import com.griot.core.design.component.ChoiceSheet

/** The language of the interface: French, English, or the phone's. */
@Composable
internal fun AppLanguageSheet(language: AppLanguage, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    ChoiceSheet(
        title = stringResource(R.string.settings_language),
        description = stringResource(R.string.settings_language_hint),
        options = listOf(
            ChoiceOption(
                value = AppLanguage.FRENCH,
                title = stringResource(R.string.settings_language_french),
                hint = stringResource(R.string.settings_language_french_hint),
            ),
            ChoiceOption(
                value = AppLanguage.ENGLISH,
                title = stringResource(R.string.settings_language_english),
                hint = stringResource(R.string.settings_language_english_hint),
            ),
            ChoiceOption(
                value = AppLanguage.SYSTEM,
                title = stringResource(R.string.settings_language_system),
                hint = stringResource(R.string.settings_language_system_hint),
            ),
        ),
        selected = language,
        onApply = onSelect,
        onDismiss = onDismiss,
    )
}

/** The name of a language of the interface. */
internal fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.SYSTEM -> R.string.settings_language_system
    AppLanguage.FRENCH -> R.string.settings_language_french
    AppLanguage.ENGLISH -> R.string.settings_language_english
}
