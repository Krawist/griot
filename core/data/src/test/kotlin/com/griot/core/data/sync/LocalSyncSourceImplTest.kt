package com.griot.core.data.sync

import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.SyncStateEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.data.model.SyncEntityType
import com.griot.core.data.model.SyncRecord
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// On an in-memory database: the rows are written by the DAOs, and read back after each sync step.
internal class LocalSyncSourceImplTest {
    private val database = inMemoryDatabase()
    private val clock = Clock.fixed(Instant.parse("2026-11-17T02:00:00Z"), ZoneOffset.UTC)
    private val source = LocalSyncSourceImpl(database = database, clock = clock)

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Pending {
        @Test
        fun `should list the rows never synced, parents first`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            database.sectionDao().insert(
                SectionEntity(
                    id = "my-section-id",
                    songId = "my-song-id",
                    kind = SectionKind.VERSE,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            database.verseDao().insert(
                VerseEntity(
                    id = "my-verse-id",
                    sectionId = "my-section-id",
                    text = "my-text",
                    position = 0,
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )

            // when
            val pending = source.pending()

            // then
            pending.map { it.type to it.id } shouldContainExactly listOf(
                SyncEntityType.SONG to "my-song-id",
                SyncEntityType.SECTION to "my-section-id",
                SyncEntityType.VERSE to "my-verse-id",
            )
        }

        @Test
        fun `should give the whole row as payload`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:30:00Z"),
                ),
            )

            // when
            val pending = source.pending()

            // then
            pending shouldContainExactly listOf(
                SyncRecord(
                    type = SyncEntityType.SONG,
                    id = "my-song-id",
                    updatedAt = Instant.parse("2026-11-17T01:30:00Z"),
                    deleted = false,
                    payload = """{"id":"my-song-id","title":"my-title","language":"FRENCH","status":"DRAFT",""" +
                        """"createdAt":"2026-11-17T01:00:00Z","updatedAt":"2026-11-17T01:30:00Z","deleted":false}""",
                ),
            )
        }

        @Test
        fun `should leave out a row not changed since its last sync`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            database.syncStateDao().upsert(
                SyncStateEntity(
                    entityType = SyncEntityType.SONG,
                    entityId = "my-song-id",
                    syncedUpdatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                    syncedAt = Instant.parse("2026-11-17T01:05:00Z"),
                ),
            )

            // when
            val pending = source.pending()

            // then
            pending.shouldBeEmpty()
        }

        @Test
        fun `should list a row changed since its last sync`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:10:00Z"),
                ),
            )
            database.syncStateDao().upsert(
                SyncStateEntity(
                    entityType = SyncEntityType.SONG,
                    entityId = "my-song-id",
                    syncedUpdatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                    syncedAt = Instant.parse("2026-11-17T01:05:00Z"),
                ),
            )

            // when
            val pending = source.pending()

            // then
            pending.map { it.id } shouldContainExactly listOf("my-song-id")
        }
    }

    @Nested
    inner class ObservePendingCount {
        @Test
        fun `should count the rows to push`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            database.sectionDao().insert(
                SectionEntity(
                    id = "my-section-id",
                    songId = "my-song-id",
                    kind = SectionKind.VERSE,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )

            // when
            val count = source.observePendingCount().first()

            // then
            count shouldBe 2
        }
    }

    @Nested
    inner class MarkPushed {
        @Test
        fun `should leave nothing to push once pushed`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            val pending = source.pending()

            // when
            source.markPushed(pending)

            // then
            source.pending().shouldBeEmpty()
            database.syncStateDao().get(entityType = SyncEntityType.SONG, entityId = "my-song-id") shouldBe
                SyncStateEntity(
                    entityType = SyncEntityType.SONG,
                    entityId = "my-song-id",
                    syncedUpdatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                    syncedAt = Instant.parse("2026-11-17T02:00:00Z"),
                )
        }

        @Test
        fun `should keep to push a row changed while it was pushed`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title-1",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            val pending = source.pending()
            database.songDao().rename(
                id = "my-song-id",
                title = "my-title-2",
                at = Instant.parse("2026-11-17T01:01:00Z"),
            )

            // when
            source.markPushed(pending)

            // then
            source.pending().map { it.id } shouldContainExactly listOf("my-song-id")
        }
    }

    @Nested
    inner class Apply {
        @Test
        fun `should add the rows missing here, a verse before its section`() = runTest {
            // given
            // The rows of another device, as it pushed them.
            val other = inMemoryDatabase()
            other.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            other.sectionDao().insert(
                SectionEntity(
                    id = "my-section-id",
                    songId = "my-song-id",
                    kind = SectionKind.VERSE,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            other.verseDao().insert(
                VerseEntity(
                    id = "my-verse-id",
                    sectionId = "my-section-id",
                    text = "my-text",
                    position = 0,
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            val records = LocalSyncSourceImpl(database = other, clock = clock).pending().reversed()
            other.close()

            // when
            source.apply(records)

            // then
            database.verseDao().getById("my-verse-id") shouldBe VerseEntity(
                id = "my-verse-id",
                sectionId = "my-section-id",
                text = "my-text",
                position = 0,
                updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
            )
            source.pending().shouldBeEmpty()
        }

        @Test
        fun `should take a more recent remote row`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title-1",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            val record = SyncRecord(
                type = SyncEntityType.SONG,
                id = "my-song-id",
                updatedAt = Instant.parse("2026-11-17T01:30:00Z"),
                deleted = false,
                payload = """{"id":"my-song-id","title":"my-title-2","language":"FRENCH","status":"DRAFT",""" +
                    """"createdAt":"2026-11-17T01:00:00Z","updatedAt":"2026-11-17T01:30:00Z","deleted":false}""",
            )

            // when
            source.apply(listOf(record))

            // then
            database.songDao().getById("my-song-id")?.title shouldBe "my-title-2"
            source.pending().shouldBeEmpty()
        }

        @Test
        fun `should keep a more recent local row, still to push`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title-1",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:40:00Z"),
                ),
            )
            val record = SyncRecord(
                type = SyncEntityType.SONG,
                id = "my-song-id",
                updatedAt = Instant.parse("2026-11-17T01:30:00Z"),
                deleted = false,
                payload = """{"id":"my-song-id","title":"my-title-2","language":"FRENCH","status":"DRAFT",""" +
                    """"createdAt":"2026-11-17T01:00:00Z","updatedAt":"2026-11-17T01:30:00Z","deleted":false}""",
            )

            // when
            source.apply(listOf(record))

            // then
            database.songDao().getById("my-song-id")?.title shouldBe "my-title-1"
            source.pending().map { it.id } shouldContainExactly listOf("my-song-id")
        }

        @Test
        fun `should take a delete from another device`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
            val record = SyncRecord(
                type = SyncEntityType.SONG,
                id = "my-song-id",
                updatedAt = Instant.parse("2026-11-17T01:30:00Z"),
                deleted = true,
                payload = """{"id":"my-song-id","title":"my-title","language":"FRENCH","status":"DRAFT",""" +
                    """"createdAt":"2026-11-17T01:00:00Z","updatedAt":"2026-11-17T01:30:00Z","deleted":true}""",
            )

            // when
            source.apply(listOf(record))

            // then
            database.songDao().getById("my-song-id")?.deleted shouldBe true
        }
    }
}
