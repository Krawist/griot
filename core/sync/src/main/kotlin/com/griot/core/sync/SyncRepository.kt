package com.griot.core.sync

import kotlinx.coroutines.flow.Flow

/**
 * Sync of the songs, versions and ideas between the phone and a [RemoteStore]. Local writes never wait for it: they
 * are saved on the phone, then pushed by the next sync.
 */
interface SyncRepository {
    /** Where sync stands, updated on every local write, network change and sync. */
    val status: Flow<SyncStatus>

    /** Pulls the changes of other devices, then pushes the phone's; one sync at a time. */
    suspend fun sync(): SyncResult
}
