package com.griot.core.sync

import com.griot.core.data.model.SyncRecord

/**
 * Where the rows of every device meet: Google Drive first (GRI-27), a Micronaut backend later. It keeps the most
 * recent record of each row and gives the records changed since a cursor of its own.
 */
interface RemoteStore {
    /** Stores [records], replacing the records of the same rows. */
    suspend fun push(records: List<SyncRecord>)

    /** Gives every record stored since [cursor], or every record when it is `null`, with the cursor to read next. */
    suspend fun pull(cursor: String?): RemoteChanges
}
