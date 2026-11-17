package com.griot.feature.library.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.TextInputForm
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.R
import com.griot.feature.library.tint

private val Swatch = 48.dp

/** The [sheet] of the folder's menu: its name, its colour, or the confirmation of its delete. */
@Composable
internal fun FolderSheetContent(sheet: FolderSheet?, folder: Folder, viewModel: FolderViewModel, onClosed: () -> Unit) {
    when (sheet) {
        FolderSheet.RENAME -> GriotSheet(onDismiss = onClosed) {
            TextInputForm(
                value = folder.name,
                label = stringResource(R.string.library_new_folder_label),
                placeholder = folder.name,
                confirmLabel = stringResource(R.string.library_rename),
                onConfirm = { name ->
                    viewModel.onRenameFolder(name)
                    onClosed()
                },
            )
        }
        FolderSheet.COLOR -> GriotSheet(onDismiss = onClosed) {
            Text(
                text = stringResource(R.string.folder_color),
                style = GriotTheme.type.sheetTitle,
                color = GriotTheme.palette.ink,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(GriotSpacing.l)) {
                listOf(FolderColor.MAGENTA, FolderColor.TURQUOISE, FolderColor.ORANGE, FolderColor.YELLOW).forEach {
                    ColorSwatch(color = it, selected = it == folder.color) {
                        viewModel.onFolderColor(it)
                        onClosed()
                    }
                }
            }
        }
        FolderSheet.DELETE -> GriotSheet(onDismiss = onClosed) {
            Text(
                text = stringResource(R.string.folder_delete_title, folder.name),
                style = GriotTheme.type.sheetTitle,
                color = GriotTheme.palette.ink,
            )
            Text(
                text = pluralStringResource(R.plurals.folder_delete_body, folder.songCount, folder.songCount),
                style = GriotTheme.type.body,
                color = GriotTheme.palette.ink2,
            )
            GriotButton(
                text = stringResource(R.string.folder_delete),
                onClick = {
                    viewModel.onDeleteFolder()
                    onClosed()
                },
            )
        }
        null -> Unit
    }
}

@Composable
private fun ColorSwatch(color: FolderColor, selected: Boolean, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val (fill, content) = color.tint()
    val description = stringResource(color.nameRes())
    Box(
        modifier = Modifier
            .size(Swatch)
            .clip(CircleShape)
            .background(fill, CircleShape)
            .then(if (selected) Modifier.border(2.dp, palette.ink, CircleShape) else Modifier)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(if (selected) DesignR.drawable.ic_check else DesignR.drawable.ic_folder),
            contentDescription = null,
            tint = content,
        )
    }
}

private fun FolderColor.nameRes(): Int = when (this) {
    FolderColor.MAGENTA -> R.string.folder_color_magenta
    FolderColor.TURQUOISE -> R.string.folder_color_turquoise
    FolderColor.ORANGE -> R.string.folder_color_orange
    FolderColor.YELLOW -> R.string.folder_color_yellow
    FolderColor.NEUTRAL -> R.string.folder_color_neutral
}
