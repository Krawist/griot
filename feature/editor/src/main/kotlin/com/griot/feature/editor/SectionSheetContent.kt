package com.griot.feature.editor

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.SectionKind
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.TextInputForm
import com.griot.core.design.theme.GriotTheme

/**
 * The [sheet] opened from the menu of [section]: the kinds of section to add below it, the field to rename it, or the
 * confirmation of its delete.
 *
 * @param label The section's label as shown.
 */
@Suppress("LongParameterList") // The sheet, the section, its label and one callback per action.
@Composable
internal fun SectionSheetContent(
    sheet: SectionSheet,
    section: EditorSection,
    label: String,
    onAddBelow: (SectionKind) -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (sheet) {
        SectionSheet.ADD_BELOW -> AddSectionSheet(onAdd = onAddBelow, onDismiss = onDismiss)
        SectionSheet.RENAME -> GriotSheet(onDismiss = onDismiss) {
            TextInputForm(
                value = section.section.name.orEmpty(),
                label = stringResource(R.string.editor_rename_label),
                placeholder = label,
                confirmLabel = stringResource(R.string.editor_rename),
                onConfirm = onRename,
            )
        }
        SectionSheet.DELETE -> GriotSheet(onDismiss = onDismiss) {
            DeleteConfirmation(section = section, label = label, onDelete = onDelete)
        }
    }
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
