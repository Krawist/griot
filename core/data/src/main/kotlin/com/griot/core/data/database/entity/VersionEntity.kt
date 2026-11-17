@file:UseSerializers(InstantSerializer::class)

package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.griot.core.data.database.InstantSerializer
import com.griot.core.data.model.VersionSource
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A past text of a verse or of a whole song, never overwritten (GRI-24).
 *
 * @property id Stable UUID, the same on every device.
 * @property songId The song it belongs to.
 * @property verseId The verse it is a version of; `null` for a version of the whole song.
 * @property text The verse's text, or the whole song as text for a song version.
 * @property source Written by the author, accepted from the AI, restored, or kept from a sync conflict.
 * @property createdAt When the version was recorded.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether the version was thinned out; the row stays so that the delete travels to other devices.
 */
@Serializable
@Entity(
    tableName = "versions",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    // No foreign key on the verse: the versions of a deleted verse stay in the song's history.
    indices = [Index("songId"), Index("verseId")],
)
internal data class VersionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val songId: String,
    val verseId: String?,
    val text: String,
    val source: VersionSource,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)
