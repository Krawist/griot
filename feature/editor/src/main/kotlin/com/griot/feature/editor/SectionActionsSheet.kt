package com.griot.feature.editor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction
import com.griot.core.design.component.TextInputForm
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/** What the sheet of a section shows: its actions, the field to rename it, or the confirmation of a delete. */
private enum class SectionSheetStep {
    ACTIONS,
    RENAME,
    DELETE,
}

/**
 * The actions of a section: rename, repeat further on, move up or down, merge into the section above, delete after a
 * confirmation.
 *
 * @param label The section's label as shown.
 */
@Suppress("LongParameterList") // The section, its label and one callback per action.
@Composable
internal fun SectionActionsSheet(
    section: EditorSection,
    label: String,
    onRename: (String) -> Unit,
    onRepeat: () -> Unit,
    onMove: (Int) -> Unit,
    onMerge: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = GriotTheme.palette
    var step by rememberSaveable { mutableStateOf(SectionSheetStep.ACTIONS) }
    GriotSheet(onDismiss = onDismiss) {
        AnimatedContent(targetState = step, label = "section actions") { current ->
            Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
                when (current) {
                    SectionSheetStep.ACTIONS -> {
                        Text(text = label, style = GriotTheme.type.sheetTitle, color = palette.ink)
                        Actions(section = section, onRename = {
                            step = SectionSheetStep.RENAME
                        }, onRepeat = onRepeat, onMove = onMove, onMerge = onMerge) {
                            step = SectionSheetStep.DELETE
                        }
                    }
                    SectionSheetStep.RENAME -> TextInputForm(
                        value = section.section.name.orEmpty(),
                        label = stringResource(R.string.editor_rename_label),
                        placeholder = label,
                        confirmLabel = stringResource(R.string.editor_rename),
                        onConfirm = onRename,
                    )
                    SectionSheetStep.DELETE -> DeleteConfirmation(section = section, label = label, onDelete = onDelete)
                }
            }
        }
    }
}

@Suppress("LongParameterList") // The section and one callback per action.
@Composable
private fun Actions(
    section: EditorSection,
    onRename: () -> Unit,
    onRepeat: () -> Unit,
    onMove: (Int) -> Unit,
    onMerge: () -> Unit,
    onDelete: () -> Unit,
) {
    // A repeat has no name of its own: it shows the label of the section it repeats.
    if (section.section.repeatOf == null) {
        SheetAction(icon = DesignR.drawable.ic_edit, label = stringResource(R.string.editor_rename), onClick = onRename)
    }
    SheetAction(icon = DesignR.drawable.ic_repeat, label = stringResource(R.string.editor_repeat), onClick = onRepeat)
    SheetAction(
        icon = DesignR.drawable.ic_arrow_up,
        label = stringResource(R.string.editor_move_up),
        onClick = { onMove(-1) },
        enabled = !section.first,
    )
    SheetAction(
        icon = DesignR.drawable.ic_arrow_down,
        label = stringResource(R.string.editor_move_down),
        onClick = { onMove(1) },
        enabled = !section.last,
    )
    if (section.mergeable) {
        SheetAction(icon = DesignR.drawable.ic_merge, label = stringResource(R.string.editor_merge), onClick = onMerge)
    }
    SheetAction(
        icon = DesignR.drawable.ic_trash,
        label = stringResource(R.string.editor_delete),
        onClick = onDelete,
        color = GriotTheme.palette.accentText,
    )
}

@Composable
private fun DeleteConfirmation(section: EditorSection, label: String, onDelete: () -> Unit) {
    val palette = GriotTheme.palette
    val verses = section.section.verses.count { it.text.isNotBlank() }
    Text(
        text = stringResource(R.string.editor_delete_title, label),
        style = GriotTheme.type.sheetTitle,
        color = palette.ink,
    )
    // Nothing to warn about for an empty section.
    val warning = when {
        section.section.repeatOf != null -> stringResource(R.string.editor_delete_repeat)
        verses > 0 -> pluralStringResource(R.plurals.editor_delete_verses, verses, verses)
        else -> null
    }
    if (warning != null) Text(text = warning, style = GriotTheme.type.body, color = palette.ink2)
    GriotButton(text = stringResource(R.string.editor_delete), onClick = onDelete)
}
