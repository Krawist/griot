package com.griot.feature.editor

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.ChoiceCard
import com.griot.core.design.component.ChoiceGroup
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotTheme

/** The writing language of the song: French, English, or automatic (the dominant language of the lyrics). */
@Composable
internal fun LanguageSheet(choice: LanguageChoice, onSelect: (LanguageChoice) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    val title = stringResource(R.string.editor_language_title)
    GriotSheet(onDismiss = onDismiss) {
        Text(text = title, style = GriotTheme.type.sheetTitle, color = palette.ink)
        ChoiceGroup(label = title) {
            ChoiceCard(
                badge = stringResource(R.string.editor_language_french),
                title = stringResource(R.string.editor_language_french_name),
                hint = stringResource(R.string.editor_language_french_hint),
                selected = choice == LanguageChoice.FRENCH,
                onClick = { onSelect(LanguageChoice.FRENCH) },
            )
            ChoiceCard(
                badge = stringResource(R.string.editor_language_english),
                title = stringResource(R.string.editor_language_english_name),
                hint = stringResource(R.string.editor_language_english_hint),
                selected = choice == LanguageChoice.ENGLISH,
                onClick = { onSelect(LanguageChoice.ENGLISH) },
            )
            ChoiceCard(
                icon = DesignR.drawable.ic_ai,
                title = stringResource(R.string.editor_language_automatic_name),
                hint = stringResource(R.string.editor_language_automatic_short),
                selected = choice == LanguageChoice.AUTOMATIC,
                onClick = { onSelect(LanguageChoice.AUTOMATIC) },
            )
        }
        Text(
            text = stringResource(R.string.editor_language_automatic_hint),
            style = GriotTheme.type.description,
            color = palette.muted,
        )
    }
}
