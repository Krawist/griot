package com.griot.core.data.model

/**
 * A verse of a [SongSection]: one line of the song.
 *
 * @property id UUID of the verse.
 * @property text The line as written.
 */
data class SongVerse(val id: String, val text: String)
