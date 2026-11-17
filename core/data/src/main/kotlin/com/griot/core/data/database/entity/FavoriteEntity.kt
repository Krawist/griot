@file:UseSerializers(InstantSerializer::class)

package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.griot.core.data.database.InstantSerializer
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A song in the author's favourites, shown in the virtual folder « Favoris » and with a heart on its card. Kept out of
 * the song's row, so that marking it does not date the song.
 *
 * @property songId The song; one row per song.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether the song left the favourites; the row stays so that it travels to other devices.
 */
@Serializable
@Entity(tableName = "favorites")
internal data class FavoriteEntity(
    @PrimaryKey val songId: String,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)
