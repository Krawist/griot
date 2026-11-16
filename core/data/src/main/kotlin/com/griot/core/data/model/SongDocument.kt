package com.griot.core.data.model

/**
 * A song as the editor shows it: its sections in order, each with its verses.
 *
 * @property id UUID of the song.
 * @property title Title; empty for a song not named yet.
 * @property language Writing language.
 * @property sections Sections that are not deleted, in order.
 */
data class SongDocument(val id: String, val title: String, val language: SongLanguage, val sections: List<SongSection>)
