@file:UseSerializers(InstantSerializer::class)

package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.griot.core.data.database.InstantSerializer
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A song: its sections and verses are in [SectionEntity] and [VerseEntity].
 *
 * @property id Stable UUID, the same on every device.
 * @property title Title shown in the library; empty for a song not named yet.
 * @property language Writing language: French, English or automatic.
 * @property status Draft or in progress.
 * @property createdAt When the song was created.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether the song is in the trash; the row stays so that the delete travels to other devices.
 */
@Serializable
@Entity(tableName = "songs", indices = [Index("updatedAt")])
internal data class SongEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val language: SongLanguage,
    val status: SongStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)
