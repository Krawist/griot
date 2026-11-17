package com.griot.core.data.model

import java.time.Instant

/**
 * A song as the library lists it.
 *
 * @property id UUID of the song.
 * @property title Title; empty for a song not named yet.
 * @property language Writing language.
 * @property status Draft or in progress.
 * @property folderIds The folders it is in; empty for « Sans dossier ».
 * @property genreId Its genre; `null` without one.
 * @property updatedAt Last change of the song.
 * @property sectionCount Number of sections, repeats included.
 * @property firstVerse First verse that is not empty; `null` for a song with no text yet.
 * @property lyrics Every verse, one per line, for the search; empty for a song with no text yet.
 * @property firstSection The verses of its first section, in order, for its rhyme scheme; empty without a section.
 */
data class LibrarySong(
    val id: String,
    val title: String,
    val language: SongLanguage,
    val status: SongStatus,
    val folderIds: List<String> = emptyList(),
    val genreId: String? = null,
    val updatedAt: Instant,
    val sectionCount: Int,
    val firstVerse: String?,
    val lyrics: String,
    val firstSection: List<String> = emptyList(),
)
