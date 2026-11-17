package com.griot.feature.ideas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.AnimatedPresence
import com.griot.core.design.component.UndoBar
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

// The undo bar floats above the bottom bar, and the list scrolls clear of both.
private val UndoBottom = 112.dp
private val ListBottom = 184.dp

/** The ideas box: an idea saved in two seconds, then every idea, newest first, free or linked to a song. */
@Composable
internal fun IdeasScreen(onOpenSong: (String) -> Unit, viewModel: IdeasViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var actionsFor by rememberSaveable { mutableStateOf<String?>(null) }
    var pickingFor by rememberSaveable { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onImagePicked(it.toString()) }
    }
    IdeasContent(
        uiState = uiState,
        viewModel = viewModel,
        onImage = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onOpenSong = onOpenSong,
        onActions = { actionsFor = it },
        onPick = { pickingFor = it },
    )
    IdeasSheets(
        uiState = uiState,
        viewModel = viewModel,
        actionsFor = actionsFor,
        onActionsClosed = { actionsFor = null },
        pickingFor = pickingFor,
        onPick = { pickingFor = it },
        onOpenSong = onOpenSong,
    )
}

// The new idea's field, the ideas, and the undo bar of a delete.
@Suppress("LongParameterList") // The state, and one callback per way to open an idea.
@Composable
private fun IdeasContent(
    uiState: IdeasUiState,
    viewModel: IdeasViewModel,
    onImage: () -> Unit,
    onOpenSong: (String) -> Unit,
    onActions: (String) -> Unit,
    onPick: (String) -> Unit,
) {
    val motion = GriotTheme.motion
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(
                start = GriotSpacing.xl,
                top = GriotSpacing.l,
                end = GriotSpacing.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.l),
        ) {
            Header()
            NewIdea(onAdd = viewModel::onAdd, onImage = onImage)
            if (!uiState.loading && uiState.items.isEmpty()) EmptyIdeas()
            LazyColumn(
                contentPadding = PaddingValues(bottom = ListBottom),
                verticalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp),
            ) {
                items(uiState.items, key = { it.idea.id }) { item ->
                    IdeaCard(
                        item = item,
                        onAction = {
                            val songId = item.idea.songId
                            if (songId != null) onOpenSong(songId) else onPick(item.idea.id)
                        },
                        onLongClick = { onActions(item.idea.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
        AnimatedPresence(
            value = uiState.deleted,
            enter = motion.barEnter(),
            exit = motion.barExit(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = GriotSpacing.l, end = GriotSpacing.l, bottom = UndoBottom),
        ) {
            UndoBar(
                message = stringResource(R.string.ideas_deleted),
                onUndo = viewModel::onUndoDelete,
                onDismissed = viewModel::onUndoDismissed,
            )
        }
    }
}

// The actions of the idea [actionsFor], and the song picker of the idea [pickingFor].
@Suppress("LongParameterList") // Which idea each sheet shows, and how it closes.
@Composable
private fun IdeasSheets(
    uiState: IdeasUiState,
    viewModel: IdeasViewModel,
    actionsFor: String?,
    onActionsClosed: () -> Unit,
    pickingFor: String?,
    onPick: (String?) -> Unit,
    onOpenSong: (String) -> Unit,
) {
    uiState.items.firstOrNull { it.idea.id == actionsFor }?.idea?.let { idea ->
        val close = onActionsClosed
        IdeaActionsSheet(
            idea = idea,
            onEdit = { text ->
                viewModel.onEdit(id = idea.id, text = text)
                close()
            },
            onLink = {
                close()
                onPick(idea.id)
            },
            onFree = {
                viewModel.onLink(id = idea.id, songId = null)
                close()
            },
            onStartSong = {
                close()
                viewModel.onStartSong(id = idea.id, onCreated = onOpenSong)
            },
            onDelete = {
                viewModel.onDelete(idea)
                close()
            },
            onDismiss = close,
        )
    }
    pickingFor?.let { id ->
        SongPickerSheet(
            songs = uiState.songs,
            onNewSong = {
                onPick(null)
                viewModel.onStartSong(id = id, onCreated = onOpenSong)
            },
            onPick = { songId ->
                viewModel.onLink(id = id, songId = songId)
                onPick(null)
            },
            onDismiss = { onPick(null) },
        )
    }
}

@Composable
private fun EmptyIdeas() {
    Text(
        text = stringResource(R.string.ideas_empty),
        style = GriotTheme.type.body,
        color = GriotTheme.palette.muted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = GriotSpacing.xxl),
    )
}

@Composable
private fun Header() {
    val palette = GriotTheme.palette
    Column(
        modifier = Modifier.padding(horizontal = GriotSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(R.string.ideas_title),
            style = GriotTheme.type.screenTitle,
            color = palette.ink,
        )
        Text(
            text = stringResource(R.string.ideas_subtitle),
            style = GriotTheme.type.description,
            color = palette.muted,
        )
    }
}

// The field of a new idea, saved on « Envoyer », and the button of an image idea.
@Composable
private fun NewIdea(onAdd: (String) -> Unit, onImage: () -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.field)
    var text by rememberSaveable { mutableStateOf("") }
    val save = {
        onAdd(text)
        text = ""
    }
    val style = GriotTheme.type.quote.copy(color = palette.ink)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .background(palette.glass, shape)
            .border(1.dp, palette.line, shape)
            .padding(start = GriotSpacing.l, end = GriotSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = style,
            cursorBrush = SolidColor(palette.accent),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(onSend = { save() }),
            modifier = Modifier.weight(1f).padding(vertical = GriotSpacing.m),
            decorationBox = { inner ->
                Box {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.ideas_placeholder),
                            style = style,
                            color = palette.muted,
                        )
                    }
                    inner()
                }
            },
        )
        NewIdeaButtons(canSave = text.isNotBlank(), onImage = onImage, onSave = save)
    }
}

// The image button, and the send button once there is text.
@Composable
private fun NewIdeaButtons(canSave: Boolean, onImage: () -> Unit, onSave: () -> Unit) {
    val palette = GriotTheme.palette
    IconButton(onClick = onImage, modifier = Modifier.size(GriotSpacing.touchTarget)) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_image),
            contentDescription = stringResource(R.string.ideas_image),
            tint = palette.muted,
        )
    }
    if (canSave) {
        IconButton(
            onClick = onSave,
            modifier = Modifier.size(GriotSpacing.touchTarget).background(palette.accentFill, CircleShape),
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_send),
                contentDescription = stringResource(R.string.ideas_send),
                tint = palette.onAccent,
            )
        }
    }
}
