package com.griot.feature.onboarding.importing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.onboarding.R

private val StepSize = 26.dp

/** How to export notes from Samsung Notes as text files, step by step, then « Choisir les fichiers ». */
@Composable
internal fun SamsungNotesHelpSheet(onPickFiles: () -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.import_samsung_help_title),
            style = GriotTheme.type.sheetTitle,
            color = palette.ink,
        )
        listOf(
            R.string.import_samsung_help_step_1,
            R.string.import_samsung_help_step_2,
            R.string.import_samsung_help_step_3,
            R.string.import_samsung_help_step_4,
        ).forEachIndexed { index, step ->
            Row(horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(StepSize).background(palette.accentSoft, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        style = GriotTheme.type.smallStrong,
                        color = palette.accentText,
                    )
                }
                Text(text = stringResource(step), style = GriotTheme.type.body, color = palette.ink2)
            }
        }
        GriotButton(text = stringResource(R.string.import_pick_files), onClick = onPickFiles)
    }
}
