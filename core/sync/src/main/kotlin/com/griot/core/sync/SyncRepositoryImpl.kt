package com.griot.core.sync

import android.util.Log
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.sync.LocalSyncSource
import java.time.Clock
import java.time.Instant
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.optionals.getOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Default implementation for [SyncRepository]; without a [RemoteStore] (until GRI-27), sync is [SyncState.OFF]. */
@Singleton
internal class SyncRepositoryImpl @Inject constructor(
    private val local: LocalSyncSource,
    private val remote: Optional<RemoteStore>,
    private val network: NetworkMonitor,
    private val preferences: UserPreferencesRepository,
    private val clock: Clock,
) : SyncRepository {
    private val running = MutableStateFlow(false)
    private val failed = MutableStateFlow(false)
    private val mutex = Mutex()

    override val status: Flow<SyncStatus> = combine(
        local.observePendingCount(),
        network.online,
        running,
        failed,
        preferences.lastSyncedAt,
    ) { pending, online, running, failed, lastSyncedAt ->
        val state = when {
            remote.isEmpty -> SyncState.OFF
            running -> SyncState.SYNCING
            !online -> SyncState.OFFLINE
            failed -> SyncState.ERROR
            pending > 0 -> SyncState.PENDING
            else -> SyncState.SYNCED
        }
        SyncStatus(state = state, pendingChanges = pending, lastSyncedAt = lastSyncedAt)
    }

    override suspend fun sync(): SyncResult {
        val store = remote.getOrNull()
        return when {
            store == null -> SyncResult.OFF
            !network.online.first() -> SyncResult.OFFLINE
            else -> mutex.withLock { syncWith(store) }
        }
    }

    // Pulls first: a row changed on both sides is settled here before the phone's version goes up.
    @Suppress("TooGenericExceptionCaught") // Any failure of the remote store keeps the changes for the next sync.
    private suspend fun syncWith(store: RemoteStore): SyncResult {
        running.value = true
        return try {
            val changes = store.pull(preferences.syncCursor.first())
            local.apply(changes.records)
            val pending = local.pending()
            if (pending.isNotEmpty()) store.push(pending)
            local.markPushed(pending)
            preferences.setSynced(at = Instant.now(clock), cursor = changes.cursor)
            failed.value = false
            SyncResult.DONE
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Sync failed, the changes stay on the phone", e)
            failed.value = true
            SyncResult.FAILED
        } finally {
            running.value = false
        }
    }

    private companion object {
        const val TAG = "Sync"
    }
}
