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
 * A song filed in a folder: a song can be in several folders.
 *
 * @property id `songId:folderId`, so that two devices filing the same song in the same folder write the same row.
 * @property songId The song.
 * @property folderId The folder.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether the song was taken out of the folder; the row stays so that it travels to other devices.
 */
@Serializable
@Entity(tableName = "song_folders", indices = [Index("songId"), Index("folderId")])
internal data class SongFolderEntity(
    @PrimaryKey val id: String,
    val songId: String,
    val folderId: String,
    val updatedAt: Instant,
    val deleted: Boolean = false,
) {
    companion object {
        /** The id of the row filing the song [songId] in the folder [folderId]. */
        fun idOf(songId: String, folderId: String) = "$songId:$folderId"
    }
}
