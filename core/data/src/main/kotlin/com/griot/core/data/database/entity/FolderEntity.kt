@file:UseSerializers(InstantSerializer::class)

package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.griot.core.data.database.InstantSerializer
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A folder of songs; songs point to it with [SongEntity.folderId].
 *
 * @property id Stable UUID, the same on every device; « archives » for the built-in folder.
 * @property name The author's name for it; empty for « Archives ».
 * @property color Its colour.
 * @property kind A folder of the author, or « Archives ».
 * @property position Its place among the folders, from 0.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether it was deleted; the row stays so that the delete travels to other devices.
 */
@Serializable
@Entity(tableName = "folders")
internal data class FolderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: FolderColor,
    val kind: FolderKind = FolderKind.CUSTOM,
    val position: Int,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)
