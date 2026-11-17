package com.griot.feature.editor

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.SongLanguage
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction
import com.griot.core.design.theme.GriotTheme

/** The writing language of the song: French, English, or automatic (the dominant language of the lyrics). */
@Composable
internal fun LanguageSheet(language: SongLanguage, onSelect: (SongLanguage) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.editor_language_title),
            style = GriotTheme.type.sheetTitle,
            color = palette.ink,
        )
        listOf(
            SongLanguage.FRENCH to R.string.editor_language_french_name,
            SongLanguage.ENGLISH to R.string.editor_language_english_name,
            SongLanguage.AUTOMATIC to R.string.editor_language_automatic_name,
        ).forEach { (option, label) ->
            val selected = option == language
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
