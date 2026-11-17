package com.griot.feature.library.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val SegmentHeight = 36.dp
private val SegmentsShape = RoundedCornerShape(14.dp)
private val SegmentShape = RoundedCornerShape(10.dp)

/** One choice among a few [options] (value and label), side by side in one track: the [selected] one filled in ink. */
@Composable
internal fun <T> Segments(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    val palette = GriotTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.glass, SegmentsShape)
            .border(1.dp, palette.line, SegmentsShape)
            .padding(GriotSpacing.xs)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs),
    ) {
        options.forEach { (value, label) ->
            val on = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(SegmentHeight)
                    .clip(SegmentShape)
                    .background(if (on) palette.inverse else palette.glass.copy(alpha = 0f), SegmentShape)
                    .selectable(selected = on, role = Role.RadioButton, onClick = { onSelect(value) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = GriotTheme.type.chip(selected = on),
                    color = if (on) palette.onInverse else palette.ink2,
                    maxLines = 1,
                )
            }
        }
    }
}

/** A group of « Filtres et tri » under its [label], in upper case. */
@Composable
internal fun Group(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
        Text(text = label.uppercase(), style = GriotTheme.type.sectionLabel, color = GriotTheme.palette.muted)
        content()
    }
}

/** Chips that wrap to the next line. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Chips(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) { content() }
}
