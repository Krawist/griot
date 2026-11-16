package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

/**
 * An idea of the ideas box: a sentence, a title or an image, free or linked to a song.
 *
 * @property id Stable UUID, the same on every device.
 * @property text The sentence or title; empty for an image alone.
 * @property imagePath Path of the image in the app's files; `null` for a text idea.
 * @property songId The song it is linked to; `null` for a free idea.
 * @property createdAt When the idea was saved.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether the idea was deleted; the row stays so that the delete travels to other devices.
 */
@Entity(
    tableName = "ideas",
    foreignKeys = [
        // A song purged from the trash frees its ideas.
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("songId"), Index("createdAt")],
)
internal data class IdeaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val text: String,
    val imagePath: String? = null,
    val songId: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)
