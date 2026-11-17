package com.griot.feature.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val ChipHeight = 34.dp

/**
 * A chip of a filter or a choice, filled when [selected].
 *
 * @param mixed Whether it holds for some of the songs selected only: outlined in magenta.
 * @param accent Whether it fills in magenta when selected, as in « Filtres et tri », rather than in ink.
 */
@Composable
internal fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    mixed: Boolean = false,
    accent: Boolean = false,
) {
    val palette = GriotTheme.palette
    val motion = GriotTheme.motion
    val fill by animateColorAsState(
        targetValue = when {
            selected && accent -> palette.accentFill
            selected -> palette.inverse
            else -> palette.glass
        },
        animationSpec = motion.colorChange(),
        label = "chip fill",
    )
    val content by animateColorAsState(
        targetValue = when {
            selected && accent -> palette.onAccent
            selected -> palette.onInverse
            else -> palette.ink2
        },
        animationSpec = motion.colorChange(),
        label = "chip text",
    )
    Box(
        modifier = Modifier
            .height(ChipHeight)
            .clip(CircleShape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .background(fill, CircleShape)
            .then(
                when {
                    selected -> Modifier
                    mixed -> Modifier.border(1.5.dp, palette.accent, CircleShape)
                    else -> Modifier.border(1.dp, palette.line, CircleShape)
                },
            )
            .padding(horizontal = GriotSpacing.m + 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = GriotTheme.type.chip(selected = selected),
            color = content,
        )
    }
}
