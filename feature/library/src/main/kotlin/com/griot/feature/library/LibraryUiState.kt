package com.griot.feature.library

import com.griot.core.data.model.Folder

/**
 * What the home screen shows.
 *
 * @property authorName Name greeted above the title; `null` when the user gave none.
 * @property query Text typed in the search field.
 * @property results The songs matching [query], last changed first; empty while it is blank.
 * @property today The last three songs changed in the last 24 hours; « Récents » holds them all.
 * @property recentCount How many songs changed in the last 24 hours: the count of « Récents ».
 * @property favoriteCount How many songs are in the author's favourites: the count of « Favoris ».
 * @property resume The last song touched, when [today] is empty; `null` otherwise or without songs.
 * @property folders The folders, « Archives » last, with their count of songs.
 * @property songCount How many songs « Tous les textes » holds.
 * @property tidyUpCount How many songs a tidy-up would change, when one is due: its card shows; 0 hides it.
 * @property loading Whether the songs are still being read, so that no empty state flashes at start.
 * @property deleted The song just moved to the trash, while the undo bar shows; `null` otherwise.
 */
internal data class LibraryUiState(
    val authorName: String? = null,
    val query: String = "",
    val results: List<LibraryItem> = emptyList(),
    val today: List<LibraryItem> = emptyList(),
    val recentCount: Int = 0,
    val favoriteCount: Int = 0,
    val resume: ResumeSong? = null,
    val folders: List<Folder> = emptyList(),
    val songCount: Int = 0,
    val tidyUpCount: Int = 0,
    val loading: Boolean = true,
    val deleted: DeletedSong? = null,
)
