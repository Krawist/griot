package com.griot.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.organisation.FolderRepository
import com.griot.core.design.component.FileShareSheet
import com.griot.core.design.component.shareFile
import com.griot.core.design.format.text

/**
 * The action sheet of the song of [item], wired to the screen's [actions]. « Ouvrir » goes on to [onOpen], « Ranger »
 * to [onFile] and its language to [onLanguage]; the others run here.
 */
@Suppress("LongParameterList") // The song, its actions, and where three of them go.
@Composable
internal fun SongActionsHost(
    item: LibraryItem,
    actions: SongActions,
    onOpen: (String) -> Unit,
    onFile: () -> Unit,
    onLanguage: (String) -> Unit,
    onClosed: () -> Unit,
) {
    val song = item.song
    val displayTitle = song.title.ifBlank { stringResource(R.string.library_untitled) }
    val copyTitle = stringResource(R.string.library_copy_title, displayTitle)
    val archived = FolderRepository.ARCHIVES_ID in song.folderIds
    val language = stringResource(
        if (song.language ==
            SongLanguage.ENGLISH
        ) {
            R.string.library_language_english
        } else {
            R.string.library_language_french
        },
    )
    val meta = listOf(
        item.meta(showFolders = true),
        language,
        stringResource(R.string.library_modified, item.updated.text()),
    )
        .filter { it.isNotEmpty() }
        .joinToString(separator = " · ")
    SongActionsSheet(
        song = song,
        meta = meta,
        onAction = { action ->
            onClosed()
            when (action) {
                SongAction.OPEN -> onOpen(song.id)
                SongAction.FILE -> onFile()
                SongAction.SHARE -> actions.onExport(id = song.id, share = true)
                SongAction.FAVORITE -> actions.onFavorite(id = song.id, favorite = !song.favorite)
                SongAction.DUPLICATE -> actions.onDuplicate(id = song.id, title = copyTitle)
                SongAction.LANGUAGE -> onLanguage(song.id)
                SongAction.EXPORT -> actions.onExport(id = song.id, share = false)
                SongAction.ARCHIVE -> actions.onArchive(id = song.id, archived = !archived)
                SongAction.DELETE -> actions.onDelete(id = song.id, title = displayTitle)
            }
        },
        onRename = { title ->
            actions.onRename(id = song.id, title = title)
            onClosed()
        },
        onDismiss = onClosed,
    )
}

/**
 * Where an exported song goes: straight to the system's share sheet (« Partager »), or to the sheet offering to share
 * or save it (« Exporter en Markdown »). Kept apart from the action sheet, which closes first.
 */
@Composable
internal fun SongExportHost(actions: SongActions) {
    val export by actions.exported.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareTitle = stringResource(com.griot.core.design.R.string.export_share)
    val current = export ?: return
    if (current.share) {
        LaunchedEffect(current) {
            val file = current.file
            shareFile(
                context = context,
                name = file.name,
                path = file.path,
                mimeType = file.mimeType,
                title = shareTitle,
            )
            actions.onExportDone()
        }
    } else {
        FileShareSheet(
            name = current.file.name,
            path = current.file.path,
            mimeType = current.file.mimeType,
            onSave = actions::onSaveExport,
            onDismiss = actions::onExportDone,
        )
    }
}
