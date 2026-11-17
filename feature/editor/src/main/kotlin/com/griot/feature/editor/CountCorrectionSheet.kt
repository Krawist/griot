package com.griot.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.RollingNumber
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val CountWidth = 64.dp

/**
 * The count of a verse holding a word the dictionaries do not know, corrected by the user: Griot keeps the syllables
 * of that [word] for every song.
 *
 * @param count The count Griot found.
 */
@Composable
internal fun CountCorrectionSheet(word: String, count: Int, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    var syllables by rememberSaveable { mutableIntStateOf(count) }
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.editor_count_title),
            style = GriotTheme.type.sheetTitle,
            color = palette.ink,
        )
        Text(
            text = stringResource(R.string.editor_count_body, word),
            style = GriotTheme.type.body,
            color = palette.ink2,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.l, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { syllables = (syllables - 1).coerceAtLeast(1) },
                modifier = Modifier.size(GriotSpacing.touchTarget),
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_minus),
                    contentDescription = stringResource(R.string.editor_count_less),
                    tint = palette.ink,
                )
            }
            RollingNumber(value = syllables, modifier = Modifier.widthIn(min = CountWidth)) {
                Text(
                    text = it.toString(),
                    style = GriotTheme.type.screenTitle,
                    color = palette.ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = CountWidth),
                )
            }
            IconButton(onClick = { syllables++ }, modifier = Modifier.size(GriotSpacing.touchTarget)) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_add),
                    contentDescription = stringResource(R.string.editor_count_more),
                    tint = palette.ink,
                )
            }
        }
        GriotButton(text = stringResource(R.string.editor_count_save), onClick = { onSave(syllables) })
    }
}
