package com.griot.core.data.model

import java.time.Instant

/**
 * One row as it travels between devices, whatever the remote store.
 *
 * @property type Kind of the row.
 * @property id UUID of the row, the same on every device.
 * @property updatedAt Last change of the row, on the device that made it; the most recent one wins.
 * @property deleted Whether the row is deleted: deletes travel as rows too.
 * @property payload The whole row as JSON, read back only by Griot.
 */
data class SyncRecord(
    val type: SyncEntityType,
    val id: String,
    val updatedAt: Instant,
    val deleted: Boolean,
    val payload: String,
)
