package com.griot.feature.editor.audio

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.AttachmentKind
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.BackButton
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.R

private val ListBottom = 120.dp

// Audio files only, in the system's picker.
private val AudioTypes = arrayOf("audio/*")

/**
 * The audios of the song [songId] (« Audios »): « Beats et musique » (files and links) and « Mémos vocaux », with
 * « Enregistrer », « Fichier » and « Lien » at the bottom. A memo or a file plays here; a link opens in its app, or in
 * the browser.
 */
@Composable
internal fun AudiosScreen(
    songId: String,
    onBack: () -> Unit,
    viewModel: AudiosViewModel = hiltViewModel<AudiosViewModel, AudiosViewModel.Factory>(
        key = "audios/$songId",
        creationCallback = { factory -> factory.create(songId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val failed by viewModel.fileFailed.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var sheet by rememberSaveable { mutableStateOf<AudioSheet?>(null) }
    var sheetFor by rememberSaveable { mutableStateOf<String?>(null) }
    var relinkFor by rememberSaveable { mutableStateOf<String?>(null) }
    var memo by rememberSaveable(stateSaver = MemoRequest.Saver) { mutableStateOf<MemoRequest?>(null) }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onFilePicked(it.toString()) }
    }
    val relinkPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val id = relinkFor
        if (uri != null && id != null) viewModel.onRelink(id = id, uri = uri.toString())
        relinkFor = null
    }
    FileFailure(failed = failed, onShown = viewModel::onFailureShown)
    val relink = { id: String ->
        relinkFor = id
        relinkPicker.launch(AudioTypes)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AudiosList(
            uiState = uiState,
            onBack = onBack,
            onOpen = { item ->
                when {
                    item.attachment.kind == AttachmentKind.LINK -> openLink(context, item.attachment.uri)
                    item.attachment.kind == AttachmentKind.FILE && (item.missing || !item.attachment.onThisPhone) ->
                        relink(item.attachment.id)
                    item.missing || !item.attachment.onThisPhone -> Unit
                    else -> viewModel.onPlayToggle(item)
                }
            },
            onActions = { id ->
                sheetFor = id
                sheet = AudioSheet.ACTIONS
            },
        )
        AddButtons(
            onRecord = { memo = MemoRequest() },
            onFile = { filePicker.launch(AudioTypes) },
            onLink = { sheet = AudioSheet.LINK },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
    MemoRecorder(songId = songId, request = memo, onClosed = { memo = null })
    AudioSheetContent(
        sheet = sheet,
        item = (uiState.beats + uiState.memos).firstOrNull { it.attachment.id == sheetFor },
        viewModel = viewModel,
        onRename = { sheet = AudioSheet.RENAME },
        onRelink = relink,
        onClosed = {
            sheet = null
            sheetFor = null
        },
    )
}

@Composable
private fun AudiosList(
    uiState: AudiosUiState,
    onBack: () -> Unit,
    onOpen: (AudioItem) -> Unit,
    onActions: (String) -> Unit,
) {
    val palette = GriotTheme.palette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = GriotSpacing.xl, top = GriotSpacing.s, end = GriotSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.l),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs),
        ) {
            BackButton(description = stringResource(R.string.audios_back), onClick = onBack)
            Column {
                Text(
                    text = stringResource(R.string.audios_title),
                    style = GriotTheme.type.sheetTitle,
                    color = palette.ink,
                )
                Text(
                    text = uiState.songTitle.ifBlank { stringResource(R.string.editor_title_placeholder) },
                    style = GriotTheme.type.small,
                    color = palette.muted,
                )
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(bottom = ListBottom),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        ) {
            if (!uiState.loading && uiState.beats.isEmpty() && uiState.memos.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.audios_empty),
                        style = GriotTheme.type.body,
                        color = palette.ink2,
                    )
                }
            }
            group(
                key = "beats",
                playing = uiState.playing,
                label = R.string.audios_beats,
                items = uiState.beats,
                onOpen = onOpen,
                onActions = onActions,
            )
            group(
                key = "memos",
                playing = uiState.playing,
                label = R.string.audios_memos,
                items = uiState.memos,
                onOpen = onOpen,
                onActions = onActions,
            )
        }
    }
}

@Suppress("LongParameterList") // The group, its audios, what plays, and the actions.
private fun androidx.compose.foundation.lazy.LazyListScope.group(
    key: String,
    playing: String?,
    label: Int,
    items: List<AudioItem>,
    onOpen: (AudioItem) -> Unit,
    onActions: (String) -> Unit,
) {
    if (items.isEmpty()) return
    item(key = key) {
        Text(
            text = stringResource(label).uppercase(),
            style = GriotTheme.type.sectionLabel,
            color = GriotTheme.palette.muted,
            modifier = Modifier.padding(start = GriotSpacing.xs, top = GriotSpacing.s),
        )
    }
    items(items = items, key = { it.attachment.id }) { item ->
        AudioRow(
            item = item,
            playing = item.attachment.uri == playing,
            onClick = { onOpen(item) },
            onLongClick = { onActions(item.attachment.id) },
        )
    }
}

// A file that could not be read is told once.
@Composable
private fun FileFailure(failed: Boolean, onShown: () -> Unit) {
    val context = LocalContext.current
    val message = stringResource(R.string.audios_file_failed)
    LaunchedEffect(failed) {
        if (failed) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onShown()
        }
    }
}

// Opens a link in its app when it is installed, otherwise in the browser.
private fun openLink(context: android.content.Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        // No app opens it: nothing to do.
    }
}

/** The icon of an audio of [kind]. */
internal fun AttachmentKind.iconRes(): Int = when (this) {
    AttachmentKind.FILE -> DesignR.drawable.ic_nav_songs
    AttachmentKind.LINK -> DesignR.drawable.ic_link
    AttachmentKind.MEMO -> DesignR.drawable.ic_mic
}
