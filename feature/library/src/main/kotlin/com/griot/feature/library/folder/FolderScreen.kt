package com.griot.feature.library.folder

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.design.component.AnimatedPresence
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.UndoBar
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.EmptyState
import com.griot.feature.library.R
import com.griot.feature.library.SongActionsHost
import com.griot.feature.library.SongCard
import com.griot.feature.library.filing.FilingSheet
import com.griot.feature.library.meta

private val SelectionLine = 2.dp
private val ListBottom = 120.dp

/**
 * The songs of the folder [folderId], or every song (« Tous les textes ») when it is `null`: genre chips and a
 * language filter, the folder's « ⋯ » menu, and « Sélectionner » to file several songs at once.
 */
@Composable
internal fun FolderScreen(
    folderId: String?,
    onBack: () -> Unit,
    onOpenSong: (String) -> Unit,
    viewModel: FolderViewModel = hiltViewModel<FolderViewModel, FolderViewModel.Factory>(
        key = folderId ?: "all",
        creationCallback = { factory -> factory.create(folderId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // A folder deleted, here or on another device, closes its screen.
    LaunchedEffect(uiState.missing) { if (uiState.missing) onBack() }
    var actionsFor by rememberSaveable { mutableStateOf<String?>(null) }
    var sheet by rememberSaveable { mutableStateOf<FolderSheet?>(null) }
    // The songs being filed, from a long press or « Ranger ».
    var filing by rememberSaveable { mutableStateOf<List<String>?>(null) }
    Box(modifier = Modifier.fillMaxSize().focusable()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = GriotSpacing.xl, top = GriotSpacing.s, end = GriotSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.m + 2.dp),
        ) {
            FolderTopBar(
                uiState = uiState,
                onBack = onBack,
                onSelecting = viewModel::onSelecting,
                onSheet = { sheet = it },
            )
            FolderFilterBar(
                uiState = uiState,
                onGenreSelected = viewModel::onGenreSelected,
                onLanguageSelected = viewModel::onLanguageSelected,
            )
            SongList(
                uiState = uiState,
                onOpenSong = { id -> if (uiState.selecting) viewModel.onToggleSelected(id) else onOpenSong(id) },
                onSongActions = { id -> if (uiState.selecting) viewModel.onToggleSelected(id) else actionsFor = id },
            )
        }
        BottomActions(
            uiState = uiState,
            onFile = { filing = uiState.selected.toList() },
            onUndoDelete = viewModel.actions::onUndoDelete,
            onUndoDismissed = viewModel.actions::onUndoDismissed,
        )
    }
    uiState.items.firstOrNull { it.song.id == actionsFor }?.let { item ->
        SongActionsHost(
            song = item.song,
            actions = viewModel.actions,
            onFile = { filing = listOf(item.song.id) },
            onClosed = { actionsFor = null },
        )
    }
    filing?.let { songIds ->
        FilingSheet(
            songIds = songIds,
            onDismiss = {
                filing = null
                viewModel.onSelecting(false)
            },
        )
    }
    uiState.folder?.let { folder ->
        FolderSheetContent(sheet = sheet, folder = folder, viewModel = viewModel, onClosed = { sheet = null })
    }
}

// The songs; in the multiple selection, the selected ones are circled.
@Composable
private fun SongList(uiState: FolderUiState, onOpenSong: (String) -> Unit, onSongActions: (String) -> Unit) {
    val palette = GriotTheme.palette
    val shape = remember { RoundedCornerShape(GriotRadius.card) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = ListBottom),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp),
    ) {
        if (!uiState.loading && uiState.items.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    title = stringResource(R.string.library_no_result_title),
                    body = stringResource(R.string.library_no_result_body),
                )
            }
        }
        items(items = uiState.items, key = { it.song.id }) { item ->
            val selected = item.song.id in uiState.selected
            SongCard(
                item = item,
                meta = item.meta(showFolders = uiState.folder == null),
                onClick = { onOpenSong(item.song.id) },
                onLongClick = { onSongActions(item.song.id) },
                modifier = Modifier
                    .animateItem()
                    .then(if (selected) Modifier.border(SelectionLine, palette.accent, shape) else Modifier),
            )
        }
    }
}

// « Ranger » while songs are selected, and the undo bar of a delete.
@Composable
private fun androidx.compose.foundation.layout.BoxScope.BottomActions(
    uiState: FolderUiState,
    onFile: () -> Unit,
    onUndoDelete: () -> Unit,
    onUndoDismissed: () -> Unit,
) {
    val motion = GriotTheme.motion
    val bottom = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(horizontal = GriotSpacing.xl, vertical = GriotSpacing.l)
    AnimatedPresence(
        value = uiState.selected.size.takeIf { uiState.selecting && it > 0 },
        enter = motion.barEnter(),
        exit = motion.barExit(),
        modifier = bottom,
    ) { GriotButton(text = stringResource(R.string.folder_file, it), onClick = onFile) }
    AnimatedPresence(value = uiState.deleted, enter = motion.barEnter(), exit = motion.barExit(), modifier = bottom) {
        UndoBar(
            message = stringResource(R.string.library_deleted, it.title),
            onUndo = onUndoDelete,
            onDismissed = onUndoDismissed,
        )
    }
}
