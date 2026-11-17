package com.griot.core.sync

import com.griot.core.data.model.SyncEntityType
import com.griot.core.data.model.SyncRecord
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import com.griot.core.data.sync.FakeLocalSyncSource
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SyncRepositoryImplTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-17T02:00:00Z"), ZoneOffset.UTC)

    @Nested
    inner class Sync {
        @Test
        fun `should pull the changes of other devices from the last cursor`() = runTest {
            // given
            val record = SyncRecord(
                type = SyncEntityType.VERSE,
                id = "my-verse-id",
                updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                deleted = false,
                payload = "my-payload",
            )
            val local = FakeLocalSyncSource()
            val remote = FakeRemoteStore(changes = RemoteChanges(records = listOf(record), cursor = "my-cursor-2"))
            val repository = SyncRepositoryImpl(
                local = local,
                remote = Optional.of(remote),
                network = FakeNetworkMonitor(),
                preferences = FakeUserPreferencesRepository(syncCursor = "my-cursor-1"),
                clock = clock,
            )

            // when
            val result = repository.sync()

            // then
            result shouldBe SyncResult.DONE
            remote.pulledFrom shouldContainExactly listOf("my-cursor-1")
            local.applied shouldContainExactly listOf(record)
        }

        @Test
        fun `should push the changes of the phone`() = runTest {
            // given
            val record = SyncRecord(
                type = SyncEntityType.SONG,
                id = "my-song-id",
                updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                deleted = false,
                payload = "my-payload",
            )
            val local = FakeLocalSyncSource(pending = listOf(record))
            val remote = FakeRemoteStore()
            val repository = SyncRepositoryImpl(
                local = local,
                remote = Optional.of(remote),
                network = FakeNetworkMonitor(),
                preferences = FakeUserPreferencesRepository(),
                clock = clock,
            )

            // when
            repository.sync()

            // then
            remote.pushed shouldContainExactly listOf(record)
            local.pushed shouldContainExactly listOf(record)
        }

        @Test
        fun `should record the time and the cursor of the sync`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            val repository = SyncRepositoryImpl(
                local = FakeLocalSyncSource(),
                remote = Optional.of(FakeRemoteStore(RemoteChanges(records = emptyList(), cursor = "my-cursor"))),
                network = FakeNetworkMonitor(),
                preferences = preferences,
                clock = clock,
            )

            // when
            repository.sync()

            // then
            preferences.lastSyncedAt.value shouldBe Instant.parse("2026-11-17T02:00:00Z")
            preferences.syncCursor.value shouldBe "my-cursor"
        }

        @Test
        fun `should do nothing without a remote store`() = runTest {
            // given
            val local = FakeLocalSyncSource()
            val repository = SyncRepositoryImpl(
                local = local,
                remote = Optional.empty(),
                network = FakeNetworkMonitor(),
                preferences = FakeUserPreferencesRepository(),
                clock = clock,
            )

            // when
            val result = repository.sync()

            // then
            result shouldBe SyncResult.OFF
            local.applied.shouldBeEmpty()
        }

        @Test
        fun `should keep the changes without network`() = runTest {
            // given
            val remote = FakeRemoteStore()
            val repository = SyncRepositoryImpl(
                local = FakeLocalSyncSource(),
                remote = Optional.of(remote),
                network = FakeNetworkMonitor(online = false),
                preferences = FakeUserPreferencesRepository(),
                clock = clock,
            )

            // when
            val result = repository.sync()

            // then
            result shouldBe SyncResult.OFFLINE
            remote.pulledFrom.shouldBeEmpty()
        }

        @Test
        fun `should keep the changes when the remote store fails`() = runTest {
            // given
            val record = SyncRecord(
                type = SyncEntityType.SONG,
                id = "my-song-id",
                updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                deleted = false,
                payload = "my-payload",
            )
            val local = FakeLocalSyncSource(pending = listOf(record))
            val preferences = FakeUserPreferencesRepository()
            val repository = SyncRepositoryImpl(
                local = local,
                remote = Optional.of(FakeRemoteStore(failing = true)),
                network = FakeNetworkMonitor(),
                preferences = preferences,
                clock = clock,
            )

            // when
            val result = repository.sync()

            // then
            result shouldBe SyncResult.FAILED
            local.pending.value shouldContainExactly listOf(record)
            preferences.lastSyncedAt.value.shouldBeNull()
        }
    }

    @Nested
    inner class Status {
        @Test
        fun `should be off without a remote store`() = runTest {
            // given
            val repository = SyncRepositoryImpl(
                local = FakeLocalSyncSource(),
                remote = Optional.empty(),
                network = FakeNetworkMonitor(),
                preferences = FakeUserPreferencesRepository(),
                clock = clock,
            )

            // when
            val status = repository.status.first()

            // then
            status shouldBe SyncStatus(state = SyncState.OFF, pendingChanges = 0, lastSyncedAt = null)
        }

        @Test
        fun `should be pending while changes wait to be pushed`() = runTest {
            // given
            val record = SyncRecord(
                type = SyncEntityType.SONG,
                id = "my-song-id",
                updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                deleted = false,
                payload = "my-payload",
            )
            val repository = SyncRepositoryImpl(
                local = FakeLocalSyncSource(pending = listOf(record)),
                remote = Optional.of(FakeRemoteStore()),
                network = FakeNetworkMonitor(),
                preferences = FakeUserPreferencesRepository(lastSyncedAt = Instant.parse("2026-11-17T00:00:00Z")),
                clock = clock,
            )

            // when
            val status = repository.status.first()

            // then
            status shouldBe SyncStatus(
                state = SyncState.PENDING,
                pendingChanges = 1,
                lastSyncedAt = Instant.parse("2026-11-17T00:00:00Z"),
            )
        }

        @Test
        fun `should be offline without network`() = runTest {
            // given
            val repository = SyncRepositoryImpl(
                local = FakeLocalSyncSource(),
                remote = Optional.of(FakeRemoteStore()),
                network = FakeNetworkMonitor(online = false),
                preferences = FakeUserPreferencesRepository(),
                clock = clock,
            )

            // when
            val status = repository.status.first()

            // then
            status.state shouldBe SyncState.OFFLINE
        }

        @Test
        fun `should be in error after a failed sync`() = runTest {
            // given
            val repository = SyncRepositoryImpl(
                local = FakeLocalSyncSource(),
                remote = Optional.of(FakeRemoteStore(failing = true)),
                network = FakeNetworkMonitor(),
                preferences = FakeUserPreferencesRepository(),
                clock = clock,
            )
            repository.sync()

            // when
            val status = repository.status.first()

            // then
            status.state shouldBe SyncState.ERROR
        }

        @Test
        fun `should be synced once everything is pushed`() = runTest {
            // given
            val repository = SyncRepositoryImpl(
                local = FakeLocalSyncSource(),
                remote = Optional.of(FakeRemoteStore()),
                network = FakeNetworkMonitor(),
                preferences = FakeUserPreferencesRepository(),
                clock = clock,
            )
            repository.sync()

            // when
            val status = repository.status.first()

            // then
            status shouldBe SyncStatus(
                state = SyncState.SYNCED,
                pendingChanges = 0,
                lastSyncedAt = Instant.parse("2026-11-17T02:00:00Z"),
            )
        }
    }
}
