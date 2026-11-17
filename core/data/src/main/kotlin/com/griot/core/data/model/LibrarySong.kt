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
 * @property genreIds Its genres; empty without one.
 * @property favorite Whether it is in the author's favourites (« Favoris », a heart on its card).
 * @property updatedAt Last change of the song.
 * @property createdAt When the song was created; its last change when not given.
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
    val genreIds: List<String> = emptyList(),
    val favorite: Boolean = false,
    val updatedAt: Instant,
    val createdAt: Instant = updatedAt,
    val sectionCount: Int,
    val firstVerse: String?,
    val lyrics: String,
    val firstSection: List<String> = emptyList(),
)
