package com.griot.feature.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val ChipHeight = 34.dp

/** The filter chips of the library: « Toutes », « Français », « English », « Brouillons ». */
@Composable
internal fun Filters(selected: LibraryFilter, onFilterSelected: (LibraryFilter) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        LibraryFilter.entries.forEach { filter ->
            FilterChip(
                label = stringResource(filter.labelRes()),
                selected = filter == selected,
                onClick = { onFilterSelected(filter) },
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val motion = GriotTheme.motion
    val fill by animateColorAsState(
        targetValue = if (selected) palette.inverse else palette.glass,
        animationSpec = motion.colorChange(),
        label = "chip fill",
    )
    val content by animateColorAsState(
        targetValue = if (selected) palette.onInverse else palette.ink2,
        animationSpec = motion.colorChange(),
        label = "chip text",
    )
    Box(
        modifier = Modifier
            .height(ChipHeight)
            .clip(CircleShape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .background(fill, CircleShape)
            .then(if (selected) Modifier else Modifier.border(1.dp, palette.line, CircleShape))
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

private fun LibraryFilter.labelRes(): Int = when (this) {
    LibraryFilter.ALL -> R.string.library_filter_all
    LibraryFilter.FRENCH -> R.string.library_filter_french
    LibraryFilter.ENGLISH -> R.string.library_filter_english
    LibraryFilter.DRAFTS -> R.string.library_filter_drafts
}
