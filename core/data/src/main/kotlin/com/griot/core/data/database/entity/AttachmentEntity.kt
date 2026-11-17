@file:UseSerializers(InstantSerializer::class)

package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.griot.core.data.database.InstantSerializer
import com.griot.core.data.model.AttachmentKind
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * An audio of a song: a memo, a file of the phone (its address only) or a link.
 *
 * @property id Stable UUID, the same on every device.
 * @property songId The song it belongs to.
 * @property sectionId The section it is about; `null` for the whole song.
 * @property verseId The verse it is about; `null` for a section or the whole song.
 * @property kind A memo, a file or a link.
 * @property uri The file's content address, or the link.
 * @property title What it is called.
 * @property durationMillis Its length in milliseconds; `null` when unknown.
 * @property deviceId The phone that holds a file (`UserPreferencesRepository.deviceId`); `null` for a link or a memo.
 * @property createdAt When it was added.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether it was removed; the row stays so that the delete travels to other devices.
 */
@Serializable
@Entity(tableName = "attachments", indices = [Index("songId")])
internal data class AttachmentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val songId: String,
    val sectionId: String? = null,
    val verseId: String? = null,
    val kind: AttachmentKind,
    val uri: String,
    val title: String,
    val durationMillis: Long? = null,
    val deviceId: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)
