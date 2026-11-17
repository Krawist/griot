package com.griot.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.griot.core.design.R
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/**
 * A sheet to pick one of [options] (« Feuille de choix unique »): its [title] and [description], one [ChoiceCard] per
 * option, the [selected] one checked; a tap checks another, and « Appliquer » gives it to [onApply].
 *
 * @param description What the choice decides, under the title; none when `null`.
 */
@Composable
fun <T> ChoiceSheet(
    title: String,
    options: List<ChoiceOption<T>>,
    selected: T,
    onApply: (T) -> Unit,
    onDismiss: () -> Unit,
    description: String? = null,
) {
    val palette = GriotTheme.palette
    var checked by remember(selected) { mutableStateOf(selected) }
    GriotSheet(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.xs)) {
            Text(text = title, style = GriotTheme.type.sheetTitle, color = palette.ink)
            description?.let { Text(text = it, style = GriotTheme.type.description, color = palette.ink2) }
        }
        ChoiceGroup(label = title, modifier = Modifier.padding(vertical = GriotSpacing.s)) {
            options.forEach { option ->
                ChoiceCard(
                    title = option.title,
                    hint = option.hint,
                    selected = option.value == checked,
                    onClick = { checked = option.value },
                )
            }
        }
        GriotButton(text = stringResource(R.string.choice_apply), onClick = { onApply(checked) })
    }
}
