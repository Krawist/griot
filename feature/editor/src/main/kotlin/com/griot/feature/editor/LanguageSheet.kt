package com.griot.feature.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.SongLanguage
import com.griot.core.design.component.ChoiceOption
import com.griot.core.design.component.ChoiceSheet

/**
 * The writing language of the song: automatic (the dominant language of the lyrics, today [language]), French or
 * English.
 */
@Composable
internal fun LanguageSheet(
    choice: LanguageChoice,
    language: SongLanguage,
    onSelect: (LanguageChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    val detected = stringResource(
        if (language ==
            SongLanguage.ENGLISH
        ) {
            R.string.editor_language_english_name
        } else {
            R.string.editor_language_french_name
        },
    )
    ChoiceSheet(
        title = stringResource(R.string.editor_language_title),
        description = stringResource(R.string.editor_language_description),
        options = listOf(
            ChoiceOption(
                value = LanguageChoice.AUTOMATIC,
                title = stringResource(R.string.editor_language_automatic_name),
                hint = stringResource(R.string.editor_language_automatic_short, detected),
            ),
            ChoiceOption(
                value = LanguageChoice.FRENCH,
                title = stringResource(R.string.editor_language_french_name),
                hint = stringResource(R.string.editor_language_french_hint),
            ),
            ChoiceOption(
                value = LanguageChoice.ENGLISH,
                title = stringResource(R.string.editor_language_english_name),
                hint = stringResource(R.string.editor_language_english_hint),
            ),
        ),
        selected = choice,
        onApply = onSelect,
        onDismiss = onDismiss,
    )
}
