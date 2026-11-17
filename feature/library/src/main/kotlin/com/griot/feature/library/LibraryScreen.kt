package com.griot.feature.library

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.design.component.AnimatedPresence
import com.griot.core.design.component.DashedAddButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.JumpToTopButton
import com.griot.core.design.component.ScrollControls
import com.griot.core.design.component.TextInputForm
import com.griot.core.design.component.TopControls
import com.griot.core.design.component.UndoBar
import com.griot.core.design.component.rememberScrollControls
import com.griot.core.design.format.RelativeTime
import com.griot.core.design.format.text
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.filing.FilingSheet
import java.time.Instant
import kotlinx.coroutines.launch

// The undo bar floats above the bottom bar; the list scrolls clear of it.
private val UndoBottom = 112.dp
private val ListBottom = 140.dp

/**
 * The home screen: the search on all texts, the songs changed in the last 24 hours (or « Reprendre » and the last
 * one touched), then the folders. Scrolling down hides the header and shows « Haut de la liste ».
 *
 * @param onOpenFolder Opens the folder with this id, or every song when `null` (« Tous les textes »).
 * @param onControlsShownChange Whether the controls show, for the app's bottom bar to follow them; `true` again when
 * the screen leaves.
 */
@Composable
internal fun LibraryScreen(
    onOpenSong: (String) -> Unit,
    onOpenFolder: (String?) -> Unit,
    onControlsShownChange: (Boolean) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val list = rememberLazyListState()
    val controls = rememberScrollControls(list)
    val currentOnControlsShownChange by rememberUpdatedState(onControlsShownChange)
    LaunchedEffect(controls) { snapshotFlow { controls.shown }.collect { currentOnControlsShownChange(it) } }
    DisposableEffect(Unit) { onDispose { currentOnControlsShownChange(true) } }
    // The song whose actions show after a long press.
    var actionsFor by rememberSaveable { mutableStateOf<String?>(null) }
    var newFolder by rememberSaveable { mutableStateOf(false) }
    var filing by rememberSaveable { mutableStateOf<String?>(null) }
    LibraryContent(
        uiState = uiState,
        list = list,
        controls = controls,
        onQueryChange = viewModel::onQueryChange,
        onOpenSong = onOpenSong,
        onSongActions = { actionsFor = it },
        onNewSong = { viewModel.onNewSong(onCreated = onOpenSong) },
        onOpenFolder = onOpenFolder,
        onNewFolder = { newFolder = true },
        onUndoDelete = viewModel.actions::onUndoDelete,
        onUndoDismissed = viewModel.actions::onUndoDismissed,
    )
    (uiState.today + uiState.results + listOfNotNull(uiState.resume?.item))
        .firstOrNull { it.song.id == actionsFor }
        ?.let { item ->
            SongActionsHost(
                song = item.song,
                actions = viewModel.actions,
                onFile = { filing = item.song.id },
                onClosed = { actionsFor = null },
            )
        }
    filing?.let { songId -> FilingSheet(songIds = listOf(songId), onDismiss = { filing = null }) }
    if (newFolder) {
        GriotSheet(onDismiss = { newFolder = false }) {
            TextInputForm(
                value = "",
                label = stringResource(R.string.library_new_folder_label),
                placeholder = stringResource(R.string.library_new_folder),
                confirmLabel = stringResource(R.string.library_create),
                onConfirm = { name ->
                    viewModel.onNewFolder(name)
                    newFolder = false
                },
            )
        }
    }
}

@Suppress("LongParameterList") // One callback per action of the home screen.
@Composable
private fun LibraryContent(
    uiState: LibraryUiState,
    list: LazyListState,
    controls: ScrollControls,
    onQueryChange: (String) -> Unit,
    onOpenSong: (String) -> Unit,
    onSongActions: (String) -> Unit,
    onNewSong: () -> Unit,
    onOpenFolder: (String?) -> Unit,
    onNewFolder: () -> Unit,
    onUndoDelete: () -> Unit,
    onUndoDismissed: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    // Focusable, so that the focus given back when a sheet closes lands here, not in the search field.
    Box(modifier = Modifier.fillMaxSize().focusable()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = GriotSpacing.xl, top = GriotSpacing.l, end = GriotSpacing.xl),
        ) {
            TopControls(shown = controls.shown) {
                Column(
                    modifier = Modifier.padding(bottom = GriotSpacing.m + 2.dp),
                    verticalArrangement = Arrangement.spacedBy(GriotSpacing.m + 2.dp),
                ) {
                    Header(authorName = uiState.authorName, onNewSong = onNewSong)
                    SearchField(query = uiState.query, onQueryChange = onQueryChange)
                }
            }
            if (!uiState.loading) {
                HomeList(
                    uiState = uiState,
                    list = list,
                    controls = controls,
                    onOpenSong = onOpenSong,
                    onSongActions = onSongActions,
                    onOpenFolder = onOpenFolder,
                    onNewFolder = onNewFolder,
                )
            }
        }
        FloatingButtons(
            deleted = uiState.deleted,
            shown = controls.shown,
            onUndoDelete = onUndoDelete,
            onUndoDismissed = onUndoDismissed,
            onJumpToTop = {
                controls.show()
                scope.launch { list.animateScrollToItem(0) }
            },
        )
    }
}

// The search's results while a text is typed; otherwise today's songs or « Reprendre », then the folders.
@Suppress("LongParameterList") // The screen, its list, and one callback per action.
@Composable
private fun HomeList(
    uiState: LibraryUiState,
    list: LazyListState,
    controls: ScrollControls,
    onOpenSong: (String) -> Unit,
    onSongActions: (String) -> Unit,
    onOpenFolder: (String?) -> Unit,
    onNewFolder: () -> Unit,
) {
    // A song changed goes to the top. The list keeps the card it showed first, now just below: when it was at the top,
    // it goes back up to show the song.
    val first = uiState.today.firstOrNull()?.song?.id
    LaunchedEffect(first) { if (list.firstVisibleItemIndex <= 1) list.scrollToItem(0) }
    LazyColumn(
        state = list,
        modifier = Modifier.fillMaxSize().nestedScroll(controls.connection),
        contentPadding = PaddingValues(bottom = ListBottom),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        when {
            uiState.query.isNotBlank() -> results(uiState.results, onOpenSong, onSongActions)
            uiState.songCount == 0 -> item(key = "empty") {
                EmptyState(
                    title = stringResource(R.string.library_empty_title),
                    body = stringResource(R.string.library_empty_body),
                )
            }
            uiState.today.isNotEmpty() -> {
                item(key = "today") { SectionLabel(text = stringResource(R.string.library_today)) }
                songs(uiState.today, onOpenSong, onSongActions)
            }
            else -> uiState.resume?.let { resume ->
                item(key = "resume") {
                    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
                        SectionLabel(text = stringResource(R.string.library_resume))
                        ResumeCard(resume = resume, onClick = { onOpenSong(resume.item.song.id) })
                    }
                }
            }
        }
        if (uiState.query.isBlank()) {
            item(key = "folders") {
                Column(
                    modifier = Modifier.padding(top = GriotSpacing.m),
                    verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
                ) {
                    FoldersTitle(songCount = uiState.songCount, onAllTexts = { onOpenFolder(null) })
                    FolderGrid(
                        folders = uiState.folders,
                        recentCount = uiState.recentCount,
                        onOpenFolder = onOpenFolder,
                    )
                    DashedAddButton(label = stringResource(R.string.library_new_folder), onClick = onNewFolder)
                }
            }
        }
    }
}

private fun LazyListScope.results(
    items: List<LibraryItem>,
    onOpenSong: (String) -> Unit,
    onSongActions: (String) -> Unit,
) {
    if (items.isEmpty()) {
        item(key = "no-result") {
            EmptyState(
                title = stringResource(R.string.library_no_result_title),
                body = stringResource(R.string.library_no_result_body),
            )
        }
    } else {
        songs(items, onOpenSong, onSongActions)
    }
}

private fun LazyListScope.songs(
    items: List<LibraryItem>,
    onOpenSong: (String) -> Unit,
    onSongActions: (String) -> Unit,
) {
    items(items = items, key = { it.song.id }) { item ->
        val motion = GriotTheme.motion
        SongCard(
            item = item,
            meta = item.meta(showFolders = true),
            onClick = { onOpenSong(item.song.id) },
            onLongClick = { onSongActions(item.song.id) },
            modifier = Modifier.animateItem(
                fadeInSpec = motion.tween(),
                placementSpec = motion.tween(),
                fadeOutSpec = motion.tween(),
            ),
        )
    }
}

// The undo bar of a delete, and « Haut de la liste » while the controls are hidden.
@Composable
private fun BoxScope.FloatingButtons(
    deleted: DeletedSong?,
    shown: Boolean,
    onUndoDelete: () -> Unit,
    onUndoDismissed: () -> Unit,
    onJumpToTop: () -> Unit,
) {
    val motion = GriotTheme.motion
    AnimatedPresence(
        value = deleted,
        enter = motion.barEnter(),
        exit = motion.barExit(),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .navigationBarsPadding()
            .padding(start = GriotSpacing.l, end = GriotSpacing.xl, bottom = UndoBottom),
    ) { song ->
        UndoBar(
            message = stringResource(R.string.library_deleted, song.title),
            onUndo = onUndoDelete,
            onDismissed = onUndoDismissed,
        )
    }
    // With the bottom bar down, « Haut de la liste » floats at the bottom of the screen.
    AnimatedVisibility(
        visible = !shown,
        enter = motion.barEnter(),
        exit = motion.barExit(),
        modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(GriotSpacing.xl),
    ) { JumpToTopButton(label = stringResource(R.string.library_jump_to_top), onClick = onJumpToTop) }
}

@Preview(name = "Light", heightDp = 844, widthDp = 390)
@Preview(name = "Dark", heightDp = 844, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LibraryScreenPreview() {
    val song = LibrarySong(
        id = "1",
        title = "Les néons",
        language = SongLanguage.FRENCH,
        status = SongStatus.IN_PROGRESS,
        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
        sectionCount = 3,
        firstVerse = "Je marche seul dans la ville",
        lyrics = "",
    )
    val folder =
        Folder(id = "f", name = "EP 2027", color = FolderColor.MAGENTA, kind = FolderKind.CUSTOM, songCount = 6)
    GriotTheme {
        Box(Modifier.background(GriotTheme.palette.background)) {
            val list = rememberLazyListState()
            LibraryContent(
                list = list,
                controls = rememberScrollControls(list),
                uiState = LibraryUiState(
                    authorName = "Krawist",
                    today = listOf(
                        LibraryItem(song = song, updated = RelativeTime.HoursAgo(2), folders = listOf(folder)),
                    ),
                    folders = listOf(
                        folder,
                        Folder(
                            id = "a",
                            name = "",
                            color = FolderColor.NEUTRAL,
                            kind = FolderKind.ARCHIVES,
                            songCount = 9,
                        ),
                    ),
                    songCount = 15,
                    loading = false,
                ),
                onQueryChange = {},
                onOpenSong = {},
                onSongActions = {},
                onNewSong = {},
                onOpenFolder = {},
                onNewFolder = {},
                onUndoDelete = {},
                onUndoDismissed = {},
            )
        }
    }
}
