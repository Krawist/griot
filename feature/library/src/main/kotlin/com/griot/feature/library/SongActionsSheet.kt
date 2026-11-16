package com.griot.feature.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction
import com.griot.core.design.component.TextInputForm
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/**
 * What a long press on a song offers: rename, duplicate and delete. « Renommer » turns the sheet into a title field.
 *
 * @param title The song's title, empty for a song not named.
 * @param displayTitle The title as shown, « Sans titre » for a song not named.
 */
@Suppress("LongParameterList") // The song's titles and one callback per action.
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
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = displayTitle,
            style = MaterialTheme.typography.titleMedium,
            color = palette.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = GriotSpacing.s),
        )
        AnimatedContent(targetState = renaming, label = "song actions") { rename ->
            if (rename) {
                TextInputForm(
                    value = title,
                    label = stringResource(R.string.library_rename_label),
                    placeholder = stringResource(R.string.library_untitled),
                    confirmLabel = stringResource(R.string.library_rename),
                    onConfirm = onRename,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
                    SheetAction(
                        icon = DesignR.drawable.ic_edit,
                        label = stringResource(R.string.library_rename),
                        onClick = { renaming = true },
                    )
                    SheetAction(
                        icon = DesignR.drawable.ic_copy,
                        label = stringResource(R.string.library_duplicate),
                        onClick = onDuplicate,
                    )
                    SheetAction(
                        icon = DesignR.drawable.ic_trash,
                        label = stringResource(R.string.library_delete),
                        onClick = onDelete,
                        color = palette.accentText,
                    )
                }
            }
        }
    }
}
