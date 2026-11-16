package com.griot.feature.library

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.AnimatedPresence
import com.griot.core.design.component.UndoBar
import com.griot.core.design.format.RelativeTime
import com.griot.core.design.format.text
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import java.time.Instant

private val SearchHeight = 46.dp
private val NewSongHeight = 52.dp
private val NewSongElevation = 10.dp

// The « Nouvelle chanson » button floats above the bottom bar; the list scrolls clear of both.
private val NewSongBottom = 112.dp
private val ListBottom = 184.dp

/** Library of songs: search, filters, the songs last changed first and « Nouvelle chanson ». */
@Composable
internal fun LibraryScreen(onOpenSong: (String) -> Unit, viewModel: LibraryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // The song whose actions show after a long press.
    var actionsFor by rememberSaveable { mutableStateOf<String?>(null) }
    LibraryContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onFilterSelected = viewModel::onFilterSelected,
        onOpenSong = onOpenSong,
        onSongActions = { actionsFor = it },
        onNewSong = { viewModel.onNewSong(onCreated = onOpenSong) },
        onUndoDelete = viewModel::onUndoDelete,
        onUndoDismissed = viewModel::onUndoDismissed,
    )
    uiState.items.firstOrNull { it.song.id == actionsFor }?.song?.let { song ->
        val displayTitle = song.title.ifBlank { stringResource(R.string.library_untitled) }
        val copyTitle = stringResource(R.string.library_copy_title, displayTitle)
        SongActionsSheet(
            title = song.title,
            displayTitle = displayTitle,
            onRename = { title ->
                viewModel.onRename(id = song.id, title = title)
                actionsFor = null
            },
            onDuplicate = {
                viewModel.onDuplicate(id = song.id, title = copyTitle)
                actionsFor = null
            },
            onDelete = {
                viewModel.onDelete(id = song.id, title = displayTitle)
                actionsFor = null
            },
            onDismiss = { actionsFor = null },
        )
    }
}

@Suppress("LongParameterList") // One callback per action of the library.
@Composable
private fun LibraryContent(
    uiState: LibraryUiState,
    onQueryChange: (String) -> Unit,
    onFilterSelected: (LibraryFilter) -> Unit,
    onOpenSong: (String) -> Unit,
    onSongActions: (String) -> Unit,
    onNewSong: () -> Unit,
    onUndoDelete: () -> Unit,
    onUndoDismissed: () -> Unit,
) {
    val motion = GriotTheme.motion
    // Focusable, so that the focus given back when a sheet closes lands here, not in the search field.
    Box(modifier = Modifier.fillMaxSize().focusable()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = GriotSpacing.xl, top = GriotSpacing.l, end = GriotSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.l),
        ) {
            Header(authorName = uiState.authorName)
            SearchField(query = uiState.query, onQueryChange = onQueryChange)
            Filters(selected = uiState.filter, onFilterSelected = onFilterSelected)
            when {
                uiState.loading -> Unit
                uiState.items.isEmpty() && !uiState.hasSongs -> EmptyState(
                    title = stringResource(R.string.library_empty_title),
                    body = stringResource(R.string.library_empty_body),
                )
                uiState.items.isEmpty() -> EmptyState(
                    title = stringResource(R.string.library_no_result_title),
                    body = stringResource(R.string.library_no_result_body),
                )
                else -> SongList(items = uiState.items, onOpenSong = onOpenSong, onSongActions = onSongActions)
            }
        }
        val floating = Modifier
            .align(Alignment.BottomEnd)
            .navigationBarsPadding()
            .padding(start = GriotSpacing.l, end = GriotSpacing.xl, bottom = NewSongBottom)
        // The undo bar takes the place of « Nouvelle chanson » while it shows.
        AnimatedPresence(
            value = uiState.deleted,
            enter = motion.barEnter(),
            exit = motion.barExit(),
            modifier = floating,
        ) { deleted ->
            UndoBar(
                message = stringResource(R.string.library_deleted, deleted.title),
                onUndo = onUndoDelete,
                onDismissed = onUndoDismissed,
            )
        }
        AnimatedVisibility(
            visible = uiState.deleted == null,
            enter = motion.barEnter(),
            exit = motion.barExit(),
            modifier = floating,
        ) {
            NewSongButton(onClick = onNewSong)
        }
    }
}

@Composable
private fun Header(authorName: String?) {
    val palette = GriotTheme.palette
    Column(
        modifier = Modifier.padding(horizontal = GriotSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (authorName != null) {
            Text(
                text = stringResource(R.string.library_greeting, authorName),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
        Text(
            text = stringResource(R.string.library_title),
            style = MaterialTheme.typography.headlineLarge,
            color = palette.ink,
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.field)
    val placeholder = stringResource(R.string.library_search)
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = palette.ink)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(palette.accent),
        modifier = Modifier
            .fillMaxWidth()
            .height(SearchHeight)
            .background(palette.glass, shape)
            .border(1.dp, palette.line, shape)
            .semantics { contentDescription = placeholder },
        decorationBox = { field ->
            Row(
                modifier = Modifier.padding(horizontal = GriotSpacing.m + 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp),
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_search),
                    contentDescription = null,
                    tint = palette.muted,
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) Text(text = placeholder, style = textStyle, color = palette.muted)
                    field()
                }
            }
        },
    )
}

@Composable
private fun SongList(items: List<LibraryItem>, onOpenSong: (String) -> Unit, onSongActions: (String) -> Unit) {
    val motion = GriotTheme.motion
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = ListBottom),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp),
    ) {
        items(items = items, key = { it.song.id }) { item ->
            SongCard(
                item = item,
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
}

@Composable
private fun EmptyState(title: String, body: String) {
    val palette = GriotTheme.palette
    Column(
        modifier = Modifier.fillMaxWidth().padding(
            top = GriotSpacing.xxl * 2,
            start = GriotSpacing.xl,
            end = GriotSpacing.xl,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = palette.ink,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NewSongButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    Row(
        modifier = modifier
            .height(NewSongHeight)
            .shadow(elevation = NewSongElevation, shape = CircleShape, spotColor = palette.accent)
            .clip(CircleShape)
            .background(palette.accentFill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = GriotSpacing.l, end = GriotSpacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        Icon(painter = painterResource(DesignR.drawable.ic_add), contentDescription = null, tint = palette.onAccent)
        Text(
            text = stringResource(R.string.library_new_song),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
            color = palette.onAccent,
        )
    }
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
    GriotTheme {
        Box(Modifier.background(GriotTheme.palette.background)) {
            LibraryContent(
                uiState = LibraryUiState(
                    authorName = "Krawist",
                    items = listOf(
                        LibraryItem(song = song, updated = RelativeTime.HoursAgo(2)),
                        LibraryItem(
                            song = song.copy(id = "2", title = "", status = SongStatus.DRAFT, firstVerse = null),
                            updated = RelativeTime.Yesterday,
                        ),
                    ),
                    hasSongs = true,
                    loading = false,
                ),
                onQueryChange = {},
                onFilterSelected = {},
                onOpenSong = {},
                onSongActions = {},
                onNewSong = {},
                onUndoDelete = {},
                onUndoDismissed = {},
            )
        }
    }
}
