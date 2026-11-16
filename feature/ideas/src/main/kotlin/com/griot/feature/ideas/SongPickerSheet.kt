package com.griot.feature.ideas

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.LibrarySong
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction
import com.griot.core.design.theme.GriotTheme

private val ListMaxHeight = 420.dp

/** The song to link an idea to: a new song from it, or one of the songs, last changed first. */
@Composable
internal fun SongPickerSheet(
    songs: List<LibrarySong>,
    onNewSong: () -> Unit,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.ideas_pick_title),
            style = MaterialTheme.typography.titleMedium,
            color = GriotTheme.palette.ink,
        )
        SheetAction(
            icon = DesignR.drawable.ic_add,
            label = stringResource(R.string.ideas_start_song),
            onClick = onNewSong,
            color = GriotTheme.palette.accentText,
        )
        LazyColumn(modifier = Modifier.heightIn(max = ListMaxHeight)) {
            items(songs, key = { it.id }) { song ->
                SheetAction(
                    icon = DesignR.drawable.ic_nav_songs,
                    label = song.title.ifBlank { stringResource(R.string.ideas_untitled_song) },
                    onClick = { onPick(song.id) },
                )
            }
        }
    }
}
