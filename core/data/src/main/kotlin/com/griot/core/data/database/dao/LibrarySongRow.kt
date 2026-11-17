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
 * @property folderId The folder it is in; `null` for « Aucun dossier ».
 * @property genreId Its genre; `null` without one.
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
    val folderId: String? = null,
    val genreId: String? = null,
    val updatedAt: Instant,
    val sectionCount: Int,
    val firstVerse: String?,
    val lyrics: String?,
)
