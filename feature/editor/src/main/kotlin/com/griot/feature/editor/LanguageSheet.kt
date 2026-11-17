package com.griot.feature.editor

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction
import com.griot.core.design.theme.GriotTheme

/** The writing language of the song: French, English, or automatic (the dominant language of the lyrics). */
@Composable
internal fun LanguageSheet(choice: LanguageChoice, onSelect: (LanguageChoice) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.editor_language_title),
            style = GriotTheme.type.sheetTitle,
            color = palette.ink,
        )
        listOf(
            LanguageChoice.FRENCH to R.string.editor_language_french_name,
            LanguageChoice.ENGLISH to R.string.editor_language_english_name,
            LanguageChoice.AUTOMATIC to R.string.editor_language_automatic_name,
        ).forEach { (option, label) ->
            val selected = option == choice
            SheetAction(
                icon = if (selected) DesignR.drawable.ic_check else DesignR.drawable.ic_circle,
                label = stringResource(label),
                onClick = { onSelect(option) },
                color = if (selected) palette.accentText else palette.ink,
            )
        }
        Text(
            text = stringResource(R.string.editor_language_automatic_hint),
            style = GriotTheme.type.description,
            color = palette.muted,
        )
    }
}
