package com.griot.core.sync

import com.griot.core.data.model.SyncRecord
import java.io.IOException

/** In-memory [RemoteStore]: gives [changes] on pull, keeps what is pushed, fails when [failing]. */
internal class FakeRemoteStore(
    private val changes: RemoteChanges = RemoteChanges(records = emptyList(), cursor = null),
    private val failing: Boolean = false,
) : RemoteStore {
    val pushed = mutableListOf<SyncRecord>()

    val pulledFrom = mutableListOf<String?>()

    override suspend fun push(records: List<SyncRecord>) {
        if (failing) throw IOException("my-message")
        pushed += records
    }

    override suspend fun pull(cursor: String?): RemoteChanges {
        pulledFrom += cursor
        if (failing) throw IOException("my-message")
        return changes
    }
}
