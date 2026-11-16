package com.griot.feature.library

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotTextField
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val ActionHeight = 56.dp

/**
 * What a long press on a song offers: rename, duplicate and delete. « Renommer » turns the sheet into a title field.
 *
 * @param title The song's title, empty for a song not named.
 * @param displayTitle The title as shown, « Sans titre » for a song not named.
 */
@Suppress("LongParameterList") // The song's titles and one callback per action.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongActionsSheet(
    title: String,
    displayTitle: String,
    onRename: (String) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = GriotTheme.palette
    var renaming by rememberSaveable { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.sheet,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.track) },
    ) {
        AnimatedContent(targetState = renaming, label = "song actions") { rename ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(start = GriotSpacing.xl, end = GriotSpacing.xl, bottom = GriotSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = GriotSpacing.s),
                )
                if (rename) {
                    RenameForm(title = title, onRename = onRename)
                } else {
                    SheetAction(icon = DesignR.drawable.ic_edit, label = stringResource(R.string.library_rename)) {
                        renaming = true
                    }
                    SheetAction(
                        icon = DesignR.drawable.ic_copy,
                        label = stringResource(R.string.library_duplicate),
                        onClick = onDuplicate,
                    )
                    SheetAction(
                        icon = DesignR.drawable.ic_trash,
                        label = stringResource(R.string.library_delete),
                        color = palette.accentText,
                        onClick = onDelete,
                    )
                }
            }
        }
    }
}

@Composable
private fun RenameForm(title: String, onRename: (String) -> Unit) {
    var value by rememberSaveable { mutableStateOf(title) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    // The field lets go of the focus before the sheet closes: otherwise it would land on the library's search field
    // and open the keyboard there.
    val rename = {
        focusManager.clearFocus()
        keyboard?.hide()
        onRename(value)
    }
    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.l)) {
        GriotTextField(
            value = value,
            onValueChange = { value = it },
            label = stringResource(R.string.library_rename_label),
            placeholder = stringResource(R.string.library_untitled),
            autoFocus = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { rename() }),
        )
        GriotButton(text = stringResource(R.string.library_rename), onClick = rename)
    }
}

@Composable
private fun SheetAction(
    @DrawableRes icon: Int,
    label: String,
    color: Color = GriotTheme.palette.ink,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ActionHeight)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.l),
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = color)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = color,
        )
    }
}
