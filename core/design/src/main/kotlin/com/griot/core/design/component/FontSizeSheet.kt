package com.griot.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.griot.core.design.R
import com.griot.core.design.theme.GriotTheme
import kotlin.math.roundToInt

/**
 * The size of the lyrics, from the editor's « Aa » and from the settings: a slider over [sizes], in sp, with a verse
 * shown at the [size] chosen.
 */
@Composable
fun FontSizeSheet(size: Int, sizes: IntRange, onSizeChange: (Int) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    val description = stringResource(R.string.font_size)
    GriotSheet(onDismiss = onDismiss) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = description, style = GriotTheme.type.sheetTitle, color = palette.ink)
            Text(
                text = stringResource(R.string.font_size_value, size),
                style = GriotTheme.type.body,
                color = palette.muted,
            )
        }
        Text(
            text = stringResource(R.string.font_size_sample),
            style = GriotTheme.type.lyrics(size = size),
            color = palette.ink,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.font_size_letter),
                style = GriotTheme.type.lyrics(size = sizes.first),
                color = palette.muted,
            )
            Slider(
                value = size.toFloat(),
                onValueChange = { onSizeChange(it.roundToInt()) },
                valueRange = sizes.first.toFloat()..sizes.last.toFloat(),
                steps = sizes.last - sizes.first - 1,
                colors = SliderDefaults.colors(
                    thumbColor = palette.accent,
                    activeTrackColor = palette.accent,
                    inactiveTrackColor = palette.track,
                    activeTickColor = palette.accent.copy(alpha = 0f),
                    inactiveTickColor = palette.track.copy(alpha = 0f),
                ),
                modifier = Modifier.weight(1f).semantics { contentDescription = description },
            )
            Text(
                text = stringResource(R.string.font_size_letter),
                style = GriotTheme.type.lyrics(size = sizes.last),
                color = palette.muted,
            )
        }
    }
}
