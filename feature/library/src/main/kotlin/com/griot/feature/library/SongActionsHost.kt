package com.griot.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.LibrarySong

/** The sheet of the [song] long pressed, wired to the screen's [actions]. */
@Composable
internal fun SongActionsHost(song: LibrarySong, actions: SongActions, onClosed: () -> Unit) {
    val displayTitle = song.title.ifBlank { stringResource(R.string.library_untitled) }
    val copyTitle = stringResource(R.string.library_copy_title, displayTitle)
    SongActionsSheet(
        title = song.title,
        displayTitle = displayTitle,
        onRename = { title ->
            actions.onRename(id = song.id, title = title)
            onClosed()
        },
        onDuplicate = {
            actions.onDuplicate(id = song.id, title = copyTitle)
            onClosed()
        },
        onDelete = {
            actions.onDelete(id = song.id, title = displayTitle)
            onClosed()
        },
        onDismiss = onClosed,
    )
}
