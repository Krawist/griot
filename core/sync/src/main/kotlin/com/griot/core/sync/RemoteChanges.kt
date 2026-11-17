package com.griot.core.sync

import com.griot.core.data.model.SyncRecord

/**
 * The records a [RemoteStore] gives since a cursor.
 *
 * @property records The records stored since the cursor, any order.
 * @property cursor Where to read from next time; `null` when the store has none and gives everything each time.
 */
data class RemoteChanges(val records: List<SyncRecord>, val cursor: String?)
