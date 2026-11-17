package com.griot.feature.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.design.component.GriotMenuItem

/**
 * The items of a section's « ⋯ » menu: add a section below, rename (not a repeat: it shows its source's label),
 * repeat, move up or down, merge into the section above, delete.
 */
@Suppress("LongParameterList") // The section and one callback per item.
@Composable
internal fun SectionMenuItems(
    section: EditorSection,
    onAddBelow: () -> Unit,
    onRename: () -> Unit,
    onRepeat: () -> Unit,
    onMove: (Int) -> Unit,
    onMerge: () -> Unit,
    onDelete: () -> Unit,
) {
    GriotMenuItem(label = stringResource(R.string.editor_add_section_below), onClick = onAddBelow)
    if (section.section.repeatOf == null) {
        GriotMenuItem(label = stringResource(R.string.editor_rename), onClick = onRename)
    }
    GriotMenuItem(label = stringResource(R.string.editor_repeat), onClick = onRepeat)
    GriotMenuItem(label = stringResource(R.string.editor_move_up), onClick = { onMove(-1) }, enabled = !section.first)
    GriotMenuItem(label = stringResource(R.string.editor_move_down), onClick = { onMove(1) }, enabled = !section.last)
    if (section.mergeable) GriotMenuItem(label = stringResource(R.string.editor_merge), onClick = onMerge)
    GriotMenuItem(label = stringResource(R.string.editor_delete), onClick = onDelete, danger = true)
}
