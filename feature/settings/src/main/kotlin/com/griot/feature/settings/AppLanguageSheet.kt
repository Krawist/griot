package com.griot.feature.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.AppLanguage
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction
import com.griot.core.design.theme.GriotTheme

/** The language of the interface: French, English, or the phone's. */
@Composable
internal fun AppLanguageSheet(language: AppLanguage, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium,
            color = palette.ink,
        )
        listOf(AppLanguage.FRENCH, AppLanguage.ENGLISH, AppLanguage.SYSTEM).forEach { option ->
            val selected = option == language
            SheetAction(
                icon = if (selected) DesignR.drawable.ic_check else DesignR.drawable.ic_circle,
                label = stringResource(option.labelRes()),
                onClick = { onSelect(option) },
                color = if (selected) palette.accentText else palette.ink,
            )
        }
        Text(
            text = stringResource(R.string.settings_language_hint),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
        )
    }
}

/** The name of a language of the interface. */
internal fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.SYSTEM -> R.string.settings_language_system
    AppLanguage.FRENCH -> R.string.settings_language_french
    AppLanguage.ENGLISH -> R.string.settings_language_english
}
