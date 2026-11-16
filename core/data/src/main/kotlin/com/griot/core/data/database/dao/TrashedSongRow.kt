package com.griot.core.data.database.dao

import java.time.Instant

/**
 * A song in the trash.
 *
 * @property id UUID of the song.
 * @property title Title; empty for a song not named.
 * @property deletedAt When the song went to the trash: its last change.
 */
internal data class TrashedSongRow(val id: String, val title: String, val deletedAt: Instant)
