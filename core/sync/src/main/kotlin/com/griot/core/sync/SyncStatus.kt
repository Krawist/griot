package com.griot.core.sync

import java.time.Instant

/**
 * What the sync pill and the settings show.
 *
 * @property state Where sync stands.
 * @property pendingChanges How many rows wait to be pushed.
 * @property lastSyncedAt When the last sync succeeded; `null` until the first one.
 */
data class SyncStatus(val state: SyncState, val pendingChanges: Int, val lastSyncedAt: Instant?)
