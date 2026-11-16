package com.griot.core.data.model

import java.time.Instant

/**
 * A song as the library lists it.
 *
 * @property id UUID of the song.
 * @property title Title; empty for a song not named yet.
 * @property language Writing language.
 * @property status Draft or in progress.
 * @property updatedAt Last change of the song.
 * @property sectionCount Number of sections, repeats included.
 * @property firstVerse First verse that is not empty; `null` for a song with no text yet.
 * @property lyrics Every verse, one per line, for the search; empty for a song with no text yet.
 */
data class LibrarySong(
    val id: String,
    val title: String,
    val language: SongLanguage,
    val status: SongStatus,
    val updatedAt: Instant,
    val sectionCount: Int,
    val firstVerse: String?,
    val lyrics: String,
)
