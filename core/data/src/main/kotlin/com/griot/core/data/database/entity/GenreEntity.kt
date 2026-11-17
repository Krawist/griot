@file:UseSerializers(InstantSerializer::class)

package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.griot.core.data.database.InstantSerializer
import com.griot.core.data.model.BuiltInGenre
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A genre; songs point to it with [SongEntity.genreId].
 *
 * @property id Stable UUID, the same on every device; the built-in genres have fixed ids.
 * @property name The author's name for it; empty for a built-in genre.
 * @property builtIn Which built-in genre it is; `null` for a genre of the author.
 * @property position Its place among the genres, from 0.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether it was deleted; the row stays so that the delete travels to other devices.
 */
@Serializable
@Entity(tableName = "genres")
internal data class GenreEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val builtIn: BuiltInGenre? = null,
    val position: Int,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)
