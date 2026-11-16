package com.griot.core.data.model

import java.time.Instant

/**
 * A song in the trash, kept [com.griot.core.data.song.SongRepository.TRASH_DAYS] days before it is removed for good.
 *
 * @property id UUID of the song.
 * @property title Title; empty for a song not named.
 * @property deletedAt When the song went to the trash.
 */
data class TrashedSong(val id: String, val title: String, val deletedAt: Instant)
