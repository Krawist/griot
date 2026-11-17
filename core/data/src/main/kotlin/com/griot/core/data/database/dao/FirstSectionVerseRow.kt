package com.griot.core.data.database.dao

/**
 * A verse of the first section of a song, for its card in the library.
 *
 * @property songId UUID of the song.
 * @property text The verse as written.
 */
internal data class FirstSectionVerseRow(val songId: String, val text: String)
