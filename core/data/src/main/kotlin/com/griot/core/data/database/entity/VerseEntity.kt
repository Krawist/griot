@file:UseSerializers(InstantSerializer::class)

package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.griot.core.data.database.InstantSerializer
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A verse: one line of the song, however long it wraps on screen.
 *
 * @property id Stable UUID, the same on every device.
 * @property sectionId The section it belongs to; never a repeat.
 * @property text The line as written, without line break.
 * @property position Rank in the section, from 0.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether the verse was deleted; the row stays so that the delete travels to other devices.
 */
@Serializable
@Entity(
    tableName = "verses",
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sectionId")],
)
internal data class VerseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sectionId: String,
    val text: String,
    val position: Int,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)
