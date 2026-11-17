package com.griot.feature.editor.audio

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.griot.core.data.model.AttachmentKind
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.GriotTextField
import com.griot.core.design.component.SheetAction
import com.griot.core.design.component.TextInputForm
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.R

/** The [sheet] of the audios screen: a new link, or the actions and the new name of the audio [item]. */
@Suppress("LongParameterList", "LongMethod") // The sheets, the audio, and how each action goes on.
@Composable
internal fun AudioSheetContent(
    sheet: AudioSheet?,
    item: AudioItem?,
    viewModel: AudiosViewModel,
    onRename: () -> Unit,
    onRelink: (String) -> Unit,
    onClosed: () -> Unit,
) {
    when (sheet) {
        AudioSheet.LINK -> GriotSheet(onDismiss = onClosed) {
            LinkForm(
                onAdd = { url, title ->
                    viewModel.onLink(url = url, title = title)
                    onClosed()
                },
            )
        }
        AudioSheet.ACTIONS -> item?.let { audio ->
            GriotSheet(onDismiss = onClosed) {
                Text(text = audio.attachment.title, style = GriotTheme.type.sheetTitle, color = GriotTheme.palette.ink)
                SheetAction(
                    icon = DesignR.drawable.ic_edit,
                    label = stringResource(R.string.audios_rename),
                    onClick = onRename,
                )
                if (audio.attachment.kind == AttachmentKind.FILE && !audio.attachment.beat) {
                    SheetAction(
                        icon = DesignR.drawable.ic_play,
                        label = stringResource(R.string.audios_set_beat),
                        onClick = {
                            viewModel.onSetBeat(audio.attachment.id)
                            onClosed()
                        },
                    )
                }
                if (audio.attachment.kind == AttachmentKind.FILE) {
                    SheetAction(
                        icon = DesignR.drawable.ic_folder,
                        label = stringResource(R.string.audios_relink),
                        onClick = {
                            onClosed()
                            onRelink(audio.attachment.id)
                        },
                    )
                }
                SheetAction(
                    icon = DesignR.drawable.ic_trash,
                    label = stringResource(R.string.audios_delete),
                    onClick = {
                        viewModel.onDelete(audio.attachment.id)
                        onClosed()
                    },
                    color = GriotTheme.palette.accentText,
                )
            }
        }
        AudioSheet.RENAME -> item?.let { audio ->
            GriotSheet(onDismiss = onClosed) {
                TextInputForm(
                    value = audio.attachment.title,
                    label = stringResource(R.string.audios_name),
                    placeholder = audio.attachment.title,
                    confirmLabel = stringResource(R.string.audios_rename),
                    onConfirm = { title ->
                        viewModel.onRename(id = audio.attachment.id, title = title)
                        onClosed()
                    },
                )
            }
        }
        null -> Unit
    }
}

// The address of the link, then an optional name.
@Composable
private fun LinkForm(onAdd: (String, String) -> Unit) {
    var url by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    Text(
        text = stringResource(R.string.audios_add_link_title),
        style = GriotTheme.type.sheetTitle,
        color = GriotTheme.palette.ink,
    )
    GriotTextField(
        value = url,
        onValueChange = { url = it },
        label = stringResource(R.string.audios_link_address),
        placeholder = stringResource(R.string.audios_link_address_hint),
        autoFocus = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
    )
    GriotTextField(
        value = title,
        onValueChange = { title = it },
        label = stringResource(R.string.audios_name),
        placeholder = stringResource(R.string.audios_link_name_hint),
    )
    GriotButton(text = stringResource(R.string.audios_add), onClick = { if (url.isNotBlank()) onAdd(url, title) })
}
