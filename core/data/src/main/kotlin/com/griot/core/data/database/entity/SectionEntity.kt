@file:UseSerializers(InstantSerializer::class)

package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.griot.core.data.database.InstantSerializer
import com.griot.core.data.model.SectionKind
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A section of a song (« Couplet 1 », « Refrain »). A repeat of another section holds no verses: it shows those of
 * its [repeatOfSectionId], so editing the chorus edits every repeat.
 *
 * @property id Stable UUID, the same on every device.
 * @property songId The song it belongs to.
 * @property kind Intro, verse, chorus, bridge, outro or other.
 * @property name Name given by the author; `null` for the automatic label of its kind and rank (« Couplet 2 »).
 * @property position Rank in the song, from 0.
 * @property repeatOfSectionId Section it repeats, in the same song; `null` for a section with its own verses.
 * @property updatedAt Last change of this row, on any device; sync keeps the most recent.
 * @property deleted Whether the section was deleted; the row stays so that the delete travels to other devices.
 */
@Serializable
@Entity(
    tableName = "sections",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
        // A repeat has nothing of its own: it goes with its source.
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["repeatOfSectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("songId"), Index("repeatOfSectionId")],
)
internal data class SectionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val songId: String,
    val kind: SectionKind,
    val name: String? = null,
    val position: Int,
    val repeatOfSectionId: String? = null,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)
