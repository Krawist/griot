@file:UseSerializers(InstantSerializer::class)

package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.griot.core.data.database.InstantSerializer
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A song tagged with a genre: a song can have several genres.
 *
 * @property id `songId:genreId`, so that two devices tagging the same song with the same genre write the same row.
 * @property songId The song.
 * @property genreId The genre.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether the genre was taken off the song; the row stays so that it travels to other devices.
 */
@Serializable
@Entity(tableName = "song_genres", indices = [Index("songId"), Index("genreId")])
internal data class SongGenreEntity(
    @PrimaryKey val id: String,
    val songId: String,
    val genreId: String,
    val updatedAt: Instant,
    val deleted: Boolean = false,
) {
    companion object {
        /** The id of the row tagging the song [songId] with the genre [genreId]. */
        fun idOf(songId: String, genreId: String) = "$songId:$genreId"
    }
}
