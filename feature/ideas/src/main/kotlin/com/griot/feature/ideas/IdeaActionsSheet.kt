package com.griot.feature.ideas

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.Idea
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction
import com.griot.core.design.component.TextInputForm
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/**
 * What a long press on an idea offers: edit it, link it to a song or free it, start a song from it, delete it.
 * « Modifier » turns the sheet into a field.
 */
@Suppress("LongParameterList") // The idea and one callback per action.
@Composable
internal fun IdeaActionsSheet(
    idea: Idea,
    onEdit: (String) -> Unit,
    onLink: () -> Unit,
    onFree: () -> Unit,
    onStartSong: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    GriotSheet(onDismiss = onDismiss) {
        AnimatedContent(targetState = editing, label = "idea actions") { edit ->
            if (edit) {
                TextInputForm(
                    value = idea.text,
                    label = stringResource(R.string.ideas_edit_label),
                    placeholder = stringResource(R.string.ideas_placeholder),
                    confirmLabel = stringResource(R.string.ideas_edit_confirm),
                    onConfirm = onEdit,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
                    if (idea.imagePath == null) {
                        SheetAction(
                            icon = DesignR.drawable.ic_edit,
                            label = stringResource(R.string.ideas_edit),
                            onClick = { editing = true },
                        )
                    }
                    SheetAction(
                        icon = DesignR.drawable.ic_link,
                        label = stringResource(R.string.ideas_link),
                        onClick = onLink,
                    )
                    if (idea.songId != null) {
                        SheetAction(
                            icon = DesignR.drawable.ic_restore,
                            label = stringResource(R.string.ideas_free_action),
                            onClick = onFree,
                        )
                    }
                    if (idea.text.isNotBlank()) {
                        SheetAction(
                            icon = DesignR.drawable.ic_add,
                            label = stringResource(R.string.ideas_start_song),
                            onClick = onStartSong,
                        )
                    }
                    SheetAction(
                        icon = DesignR.drawable.ic_trash,
                        label = stringResource(R.string.ideas_delete),
                        onClick = onDelete,
                        color = GriotTheme.palette.accentText,
                    )
                }
            }
        }
    }
}
