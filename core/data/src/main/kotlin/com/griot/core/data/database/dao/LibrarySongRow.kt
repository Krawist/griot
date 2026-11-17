package com.griot.core.data.database.dao

import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import java.time.Instant

/**
 * One song of the library, read in one query with what its card shows.
 *
 * @property id UUID of the song.
 * @property title Title; empty for a song not named yet.
 * @property language Writing language.
 * @property status Draft or in progress.
 * @property folderIds The ids of the folders it is in, separated by commas; `null` when it is in none.
 * @property genreIds The ids of its genres, separated by commas; `null` when it has none.
 * @property favorite Whether it is in the author's favourites.
 * @property createdAt When the song was created.
 * @property updatedAt Last change of the song.
 * @property sectionCount Number of sections that are not deleted, repeats included.
 * @property firstVerse First verse that is not empty, in the order of the song; `null` when there is none.
 * @property lyrics Every verse that is not deleted, one per line, in no particular order; `null` without verses.
 */
internal data class LibrarySongRow(
    val id: String,
    val title: String,
    val language: SongLanguage,
    val status: SongStatus,
    val folderIds: String? = null,
    val genreIds: String? = null,
    val favorite: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val sectionCount: Int,
    val firstVerse: String?,
    val lyrics: String?,
)
