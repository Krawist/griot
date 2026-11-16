package com.griot.core.data.database.entity

import androidx.room.Entity
import com.griot.core.data.model.SyncEntityType
import java.time.Instant

/**
 * What sync last exchanged for one row: the row has changes to push when its `updatedAt` is after [syncedUpdatedAt],
 * or when it has no sync state yet.
 *
 * @property entityType Kind of the row: song, section, verse, version or idea.
 * @property entityId UUID of the row.
 * @property syncedUpdatedAt The row's `updatedAt` when it was last pushed or pulled.
 * @property syncedAt When that sync happened.
 */
@Entity(tableName = "sync_state", primaryKeys = ["entityType", "entityId"])
internal data class SyncStateEntity(
    val entityType: SyncEntityType,
    val entityId: String,
    val syncedUpdatedAt: Instant,
    val syncedAt: Instant,
)
