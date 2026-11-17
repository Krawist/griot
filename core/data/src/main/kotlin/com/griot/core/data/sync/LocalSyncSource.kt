package com.griot.core.data.sync

import com.griot.core.data.model.SyncRecord
import kotlinx.coroutines.flow.Flow

/** The phone's side of sync: the rows to push, and how the rows of other devices are taken in. */
interface LocalSyncSource {
    /** How many rows have changes to push, updated on every write. */
    fun observePendingCount(): Flow<Int>

    /** Every row with changes to push, deleted ones included, parents first: songs, sections, verses, versions… */
    suspend fun pending(): List<SyncRecord>

    /** Records that [records] reached the remote store as they were read: a row changed since stays to push. */
    suspend fun markPushed(records: List<SyncRecord>)

    /**
     * Takes in the rows changed on other devices, in one transaction: a row missing here is added, and the most recent
     * of the two texts of a row wins.
     */
    suspend fun apply(records: List<SyncRecord>)
}
