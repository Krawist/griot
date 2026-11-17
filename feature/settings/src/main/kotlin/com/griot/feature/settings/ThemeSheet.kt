package com.griot.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.ThemeMode
import com.griot.core.design.component.ChoiceOption
import com.griot.core.design.component.ChoiceSheet

/** Whether Griot is light, dark, or as the phone. */
@Composable
internal fun ThemeSheet(mode: ThemeMode, onSelect: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    ChoiceSheet(
        title = stringResource(R.string.settings_theme),
        options = ThemeMode.entries.map { option ->
            ChoiceOption(
                value = option,
                title = stringResource(option.labelRes()),
                hint = stringResource(option.hintRes()),
            )
        },
        selected = mode,
        onApply = onSelect,
        onDismiss = onDismiss,
    )
}

/** The name of a theme. */
internal fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

private fun ThemeMode.hintRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system_hint
    ThemeMode.LIGHT -> R.string.settings_theme_light_hint
    ThemeMode.DARK -> R.string.settings_theme_dark_hint
}
