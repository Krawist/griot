package com.griot.feature.library.folder

import com.griot.core.data.model.Folder
import com.griot.core.data.model.Genre
import com.griot.core.data.model.SongLanguage
import com.griot.feature.library.DeletedSong
import com.griot.feature.library.LibraryItem

/**
 * What the folder screen shows: a folder's songs, or every song (« Tous les textes »).
 *
 * @property folder The folder shown; `null` for « Tous les textes », « Récents » and « Favoris ».
 * @property virtual [com.griot.feature.library.RECENT_FOLDER_ID] or [com.griot.feature.library.FAVORITES_FOLDER_ID]
 *   for « Récents » (the songs changed in the last 24 hours) or « Favoris »; `null` otherwise.
 * @property songCount How many songs the folder (or the library) holds, before the filters.
 * @property genres The genres its songs have, in the genres' order: the chips after « Tous ».
 * @property genre The id of the genre chip selected; `null` for « Tous ».
 * @property language The language filter; `null` for « Toutes ».
 * @property items The songs under the filters, last changed first.
 * @property selecting Whether the multiple selection is on.
 * @property selected The ids of the songs selected.
 * @property loading Whether the songs are still being read.
 * @property missing Whether the folder was deleted, here or on another device: the screen closes.
 * @property deleted The song just moved to the trash, while the undo bar shows; `null` otherwise.
 */
internal data class FolderUiState(
    val folder: Folder? = null,
    val virtual: String? = null,
    val songCount: Int = 0,
    val genres: List<Genre> = emptyList(),
    val genre: String? = null,
    val language: SongLanguage? = null,
    val items: List<LibraryItem> = emptyList(),
    val selecting: Boolean = false,
    val selected: Set<String> = emptySet(),
    val loading: Boolean = true,
    val missing: Boolean = false,
    val deleted: DeletedSong? = null,
)
