package com.griot.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.AppLanguage
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.ChoiceCard
import com.griot.core.design.component.ChoiceGroup
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotTheme

/** The language of the interface: French, English, or the phone's, one card each. */
@Composable
internal fun AppLanguageSheet(language: AppLanguage, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    val title = stringResource(R.string.settings_language)
    GriotSheet(onDismiss = onDismiss) {
        Text(text = title, style = GriotTheme.type.sheetTitle, color = palette.ink)
        ChoiceGroup(label = title) {
            ChoiceCard(
                badge = stringResource(R.string.settings_language_french_code),
                title = stringResource(R.string.settings_language_french),
                hint = stringResource(R.string.settings_language_french_hint),
                selected = language == AppLanguage.FRENCH,
                onClick = { onSelect(AppLanguage.FRENCH) },
            )
            ChoiceCard(
                badge = stringResource(R.string.settings_language_english_code),
                title = stringResource(R.string.settings_language_english),
                hint = stringResource(R.string.settings_language_english_hint),
                selected = language == AppLanguage.ENGLISH,
                onClick = { onSelect(AppLanguage.ENGLISH) },
            )
            ChoiceCard(
                icon = DesignR.drawable.ic_phone,
                title = stringResource(R.string.settings_language_system),
                hint = stringResource(R.string.settings_language_system_hint),
                selected = language == AppLanguage.SYSTEM,
                onClick = { onSelect(AppLanguage.SYSTEM) },
            )
        }
        Text(
            text = stringResource(R.string.settings_language_hint),
            style = GriotTheme.type.description,
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
