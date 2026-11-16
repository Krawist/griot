package com.griot.core.data.database.dao

import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.SyncStateEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.SyncEntityType
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SyncStateDaoTest {
    private lateinit var database: GriotDatabase
    private lateinit var syncStateDao: SyncStateDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        syncStateDao = database.syncStateDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Get {
        @Test
        fun `should return null for a row that never synced`() = runTest {
            // when
            val state = syncStateDao.get(entityType = SyncEntityType.SONG, entityId = "my-entity-id")

            // then
            state.shouldBeNull()
        }

        @Test
        fun `should tell rows of different kinds with the same id apart`() = runTest {
            // given
            val verseState = SyncStateEntity(
                entityType = SyncEntityType.VERSE,
                entityId = "my-entity-id",
                syncedUpdatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                syncedAt = Instant.parse("2026-11-16T10:01:00Z"),
            )
            syncStateDao.upsert(verseState)

            // when
            val state = syncStateDao.get(entityType = SyncEntityType.SONG, entityId = "my-entity-id")

            // then
            state.shouldBeNull()
        }
    }

    @Nested
    inner class Upsert {
        @Test
        fun `should replace the state of the row`() = runTest {
            // given
            syncStateDao.upsert(
                SyncStateEntity(
                    entityType = SyncEntityType.SONG,
                    entityId = "my-entity-id",
                    syncedUpdatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    syncedAt = Instant.parse("2026-11-16T10:01:00Z"),
                ),
            )
            val newer = SyncStateEntity(
                entityType = SyncEntityType.SONG,
                entityId = "my-entity-id",
                syncedUpdatedAt = Instant.parse("2026-11-16T11:00:00Z"),
                syncedAt = Instant.parse("2026-11-16T11:01:00Z"),
            )

            // when
            syncStateDao.upsert(newer)

            // then
            syncStateDao.get(entityType = SyncEntityType.SONG, entityId = "my-entity-id") shouldBe newer
        }
    }
}
