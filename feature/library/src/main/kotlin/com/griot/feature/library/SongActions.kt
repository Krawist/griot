package com.griot.feature.library

import com.griot.core.data.song.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * What a long press on a song does, the same on every screen of songs: rename, duplicate, delete with an undo bar.
 * Each screen's ViewModel holds one, running in its [scope].
 */
internal class SongActions(private val songs: SongRepository, private val scope: CoroutineScope) {
    private val deletedSong = MutableStateFlow<DeletedSong?>(null)

    /** The song just moved to the trash, while the undo bar shows; `null` otherwise. */
    val deleted: StateFlow<DeletedSong?> = deletedSong.asStateFlow()

    fun onRename(id: String, title: String) {
        scope.launch { songs.renameSong(id = id, title = title.trim()) }
    }

    /** Puts the song in the favourites, or takes it out when [favorite] is `false`. */
    fun onFavorite(id: String, favorite: Boolean) {
        scope.launch { songs.setFavorite(ids = listOf(id), favorite = favorite) }
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
