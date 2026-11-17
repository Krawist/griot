package com.griot.feature.library

import com.griot.core.data.export.MarkdownExporter
import com.griot.core.data.organisation.FolderRepository
import com.griot.core.data.song.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * What a long press on a song does, the same on every screen of songs: favourite, rename, duplicate, archive, export or
 * share, delete with an undo bar. Each screen's ViewModel holds one, running in its [scope].
 */
internal class SongActions(
    private val songs: SongRepository,
    private val folders: FolderRepository,
    private val exporter: MarkdownExporter,
    private val scope: CoroutineScope,
) {
    private val deletedSong = MutableStateFlow<DeletedSong?>(null)
    private val exportedSong = MutableStateFlow<SongExport?>(null)

    /** The song exported, while it waits to be shared or saved; `null` otherwise. */
    val exported: StateFlow<SongExport?> = exportedSong.asStateFlow()

    /** The song just moved to the trash, while the undo bar shows; `null` otherwise. */
    val deleted: StateFlow<DeletedSong?> = deletedSong.asStateFlow()

    fun onRename(id: String, title: String) {
        scope.launch { songs.renameSong(id = id, title = title.trim()) }
    }

    /** Puts the song in the favourites, or takes it out when [favorite] is `false`. */
    fun onFavorite(id: String, favorite: Boolean) {
        scope.launch { songs.setFavorite(ids = listOf(id), favorite = favorite) }
    }

    /** Files the song in « Archives », or takes it out when [archived] is `false`. */
    fun onArchive(id: String, archived: Boolean) {
        val archives = setOf(FolderRepository.ARCHIVES_ID)
        scope.launch {
            if (archived) {
                folders.fileSongs(songIds = listOf(id), add = archives, remove = emptySet())
            } else {
                folders.fileSongs(songIds = listOf(id), add = emptySet(), remove = archives)
            }
        }
    }

    /** Exports the song as Markdown, then shares it at once when [share], or offers to share or save it. */
    fun onExport(id: String, share: Boolean) {
        scope.launch { exportedSong.value = exporter.exportSong(id)?.let { SongExport(file = it, share = share) } }
    }

    /** Saves the exported song in the document [uri] the user picked. */
    fun onSaveExport(uri: String) {
        val export = exportedSong.value ?: return
        scope.launch { exporter.save(file = export.file, uri = uri) }
    }

    /** The exported song left the app, or the author closed its sheet. */
    fun onExportDone() {
        exportedSong.value = null
    }

    /** Copies the song under [title], such as « Les néons (copie) ». */
    fun onDuplicate(id: String, title: String) {
        scope.launch { songs.duplicateSong(id = id, title = title) }
    }

    /** Moves the song to the trash and shows the undo bar; [title] is the one the bar shows. */
    fun onDelete(id: String, title: String) {
        scope.launch {
            songs.deleteSong(id)
            deletedSong.value = DeletedSong(id = id, title = title)
        }
    }

    /** Takes the song just deleted out of the trash. */
    fun onUndoDelete() {
        val song = deletedSong.value ?: return
        deletedSong.value = null
        scope.launch { songs.restoreSong(song.id) }
    }

    fun onUndoDismissed() {
        deletedSong.value = null
    }
}
