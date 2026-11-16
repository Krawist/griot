package com.griot.feature.library

/**
 * The song just moved to the trash, which the undo bar can bring back.
 *
 * @property id UUID of the song.
 * @property title Title as the undo bar shows it, « Sans titre » for a song not named.
 */
internal data class DeletedSong(val id: String, val title: String)
