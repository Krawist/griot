package com.griot.core.data.sync

import com.griot.core.data.model.SyncRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [LocalSyncSource] for tests: [pending] rows to push, and what was pushed and applied. */
class FakeLocalSyncSource(pending: List<SyncRecord> = emptyList()) : LocalSyncSource {
    val pending = MutableStateFlow(pending)

    val pushed = mutableListOf<SyncRecord>()

    val applied = mutableListOf<SyncRecord>()

    override fun observePendingCount(): Flow<Int> = pending.map { it.size }

    override suspend fun pending(): List<SyncRecord> = pending.value

    override suspend fun markPushed(records: List<SyncRecord>) {
        pushed += records
        pending.value -= records.toSet()
    }

    override suspend fun apply(records: List<SyncRecord>) {
        applied += records
    }
}
