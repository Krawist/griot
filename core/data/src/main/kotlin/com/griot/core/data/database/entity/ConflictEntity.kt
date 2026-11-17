package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A notice of a sync conflict, until the author looks at it (GRI-28). It stays on this phone: it does not sync.
 *
 * @property versionId The version keeping the text that lost, of source « Autre appareil ».
 * @property songId The song of the verse.
 * @property verseId The verse changed on two devices.
 * @property createdAt When the conflict was settled.
 */
@Entity(
    tableName = "sync_conflicts",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("songId")],
)
internal data class ConflictEntity(
    @PrimaryKey val versionId: String,
    val songId: String,
    val verseId: String,
    val createdAt: Instant,
)
