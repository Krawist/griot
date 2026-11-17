package com.griot.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.ThemeMode
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.ChoiceCard
import com.griot.core.design.component.ChoiceGroup
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotTheme

/** Whether Griot is light, dark, or as the phone, one card each; the choice applies at once. */
@Composable
internal fun ThemeSheet(mode: ThemeMode, onSelect: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    val title = stringResource(R.string.settings_theme)
    GriotSheet(onDismiss = onDismiss) {
        Text(text = title, style = GriotTheme.type.sheetTitle, color = GriotTheme.palette.ink)
        ChoiceGroup(label = title) {
            ThemeMode.entries.forEach { option ->
                ChoiceCard(
                    icon = option.iconRes(),
                    title = stringResource(option.labelRes()),
                    hint = stringResource(option.hintRes()),
                    selected = option == mode,
                    onClick = { onSelect(option) },
                )
            }
        }
    }
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

private fun ThemeMode.iconRes(): Int = when (this) {
    ThemeMode.SYSTEM -> DesignR.drawable.ic_phone
    ThemeMode.LIGHT -> DesignR.drawable.ic_sun
    ThemeMode.DARK -> DesignR.drawable.ic_moon
}
