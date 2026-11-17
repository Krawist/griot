package com.griot.core.data.sync

import com.griot.core.data.database.entity.IdeaEntity
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
import com.griot.core.data.model.VersionSource
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
                    payload =
                    """{"id":"my-song-id","title":"my-title","language":"FRENCH","languageDetected":false,""" +
                        """"status":"DRAFT","genreId":null,"createdAt":"2026-11-17T01:00:00Z",""" +
                        """"updatedAt":"2026-11-17T01:30:00Z",""" +
                        """"deleted":false}""",
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

    @Nested
    inner class ApplyConflict {
        @BeforeEach
        fun setUp() = runTest {
            // Every test works on one verse, synced at 01:00 then changed here at 01:10.
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
                    text = "my-text-1",
                    position = 0,
                    updatedAt = Instant.parse("2026-11-17T01:10:00Z"),
                ),
            )
            database.syncStateDao().upsert(
                SyncStateEntity(
                    entityType = SyncEntityType.VERSE,
                    entityId = "my-verse-id",
                    syncedUpdatedAt = Instant.parse("2026-11-17T01:00:00Z"),
                    syncedAt = Instant.parse("2026-11-17T01:00:00Z"),
                ),
            )
        }

        @Test
        fun `should keep the text of the phone as a version when the other device wrote last`() = runTest {
            // given
            val record = SyncRecord(
                type = SyncEntityType.VERSE,
                id = "my-verse-id",
                updatedAt = Instant.parse("2026-11-17T01:20:00Z"),
                deleted = false,
                payload = """{"id":"my-verse-id","sectionId":"my-section-id","text":"my-text-2","position":0,""" +
                    """"updatedAt":"2026-11-17T01:20:00Z","deleted":false}""",
            )

            // when
            source.apply(listOf(record))

            // then
            database.verseDao().getById("my-verse-id")?.text shouldBe "my-text-2"
            val versions = database.versionDao().observeByVerse("my-verse-id").first()
            versions.map { it.text to it.source } shouldContainExactly listOf(
                "my-text-1" to VersionSource.SYNC_CONFLICT,
            )
        }

        @Test
        fun `should keep the text of the other device as a version when the phone wrote last`() = runTest {
            // given
            val record = SyncRecord(
                type = SyncEntityType.VERSE,
                id = "my-verse-id",
                updatedAt = Instant.parse("2026-11-17T01:05:00Z"),
                deleted = false,
                payload = """{"id":"my-verse-id","sectionId":"my-section-id","text":"my-text-2","position":0,""" +
                    """"updatedAt":"2026-11-17T01:05:00Z","deleted":false}""",
            )

            // when
            source.apply(listOf(record))

            // then
            database.verseDao().getById("my-verse-id")?.text shouldBe "my-text-1"
            val versions = database.versionDao().observeByVerse("my-verse-id").first()
            versions.map { it.text to it.source } shouldContainExactly listOf(
                "my-text-2" to VersionSource.SYNC_CONFLICT,
            )
        }

        @Test
        fun `should tell of the conflict on the verse`() = runTest {
            // given
            val record = SyncRecord(
                type = SyncEntityType.VERSE,
                id = "my-verse-id",
                updatedAt = Instant.parse("2026-11-17T01:20:00Z"),
                deleted = false,
                payload = """{"id":"my-verse-id","sectionId":"my-section-id","text":"my-text-2","position":0,""" +
                    """"updatedAt":"2026-11-17T01:20:00Z","deleted":false}""",
            )

            // when
            source.apply(listOf(record))

            // then
            database.conflictDao().observeVersesBySong("my-song-id").first() shouldContainExactly listOf("my-verse-id")
        }

        @Test
        fun `should keep the text of the phone when the other device deleted the verse`() = runTest {
            // given
            val record = SyncRecord(
                type = SyncEntityType.VERSE,
                id = "my-verse-id",
                updatedAt = Instant.parse("2026-11-17T01:20:00Z"),
                deleted = true,
                payload = """{"id":"my-verse-id","sectionId":"my-section-id","text":"my-text-0","position":0,""" +
                    """"updatedAt":"2026-11-17T01:20:00Z","deleted":true}""",
            )

            // when
            source.apply(listOf(record))

            // then
            database.verseDao().getById("my-verse-id")?.deleted shouldBe true
            database.versionDao().observeByVerse("my-verse-id").first().map { it.text } shouldContainExactly
                listOf("my-text-1")
        }

        @Test
        fun `should make no conflict when the same text was written on both devices`() = runTest {
            // given
            val record = SyncRecord(
                type = SyncEntityType.VERSE,
                id = "my-verse-id",
                updatedAt = Instant.parse("2026-11-17T01:20:00Z"),
                deleted = false,
                payload = """{"id":"my-verse-id","sectionId":"my-section-id","text":"my-text-1","position":0,""" +
                    """"updatedAt":"2026-11-17T01:20:00Z","deleted":false}""",
            )

            // when
            source.apply(listOf(record))

            // then
            database.versionDao().observeByVerse("my-verse-id").first().shouldBeEmpty()
            database.conflictDao().observeVersesBySong("my-song-id").first().shouldBeEmpty()
        }

        @Test
        fun `should make no conflict when the phone did not change the verse since its last sync`() = runTest {
            // given
            database.syncStateDao().upsert(
                SyncStateEntity(
                    entityType = SyncEntityType.VERSE,
                    entityId = "my-verse-id",
                    syncedUpdatedAt = Instant.parse("2026-11-17T01:10:00Z"),
                    syncedAt = Instant.parse("2026-11-17T01:10:00Z"),
                ),
            )
            val record = SyncRecord(
                type = SyncEntityType.VERSE,
                id = "my-verse-id",
                updatedAt = Instant.parse("2026-11-17T01:20:00Z"),
                deleted = false,
                payload = """{"id":"my-verse-id","sectionId":"my-section-id","text":"my-text-2","position":0,""" +
                    """"updatedAt":"2026-11-17T01:20:00Z","deleted":false}""",
            )

            // when
            source.apply(listOf(record))

            // then
            database.verseDao().getById("my-verse-id")?.text shouldBe "my-text-2"
            database.versionDao().observeByVerse("my-verse-id").first().shouldBeEmpty()
        }

        @Test
        fun `should keep both texts of an idea changed on both devices`() = runTest {
            // given
            database.ideaDao().insert(
                IdeaEntity(
                    id = "my-idea-id",
                    text = "my-text-1",
                    createdAt = Instant.parse("2026-11-17T01:00:00Z"),
                    updatedAt = Instant.parse("2026-11-17T01:10:00Z"),
                ),
            )
            val record = SyncRecord(
                type = SyncEntityType.IDEA,
                id = "my-idea-id",
                updatedAt = Instant.parse("2026-11-17T01:20:00Z"),
                deleted = false,
                payload = """{"id":"my-idea-id","text":"my-text-2","imagePath":null,"songId":null,""" +
                    """"createdAt":"2026-11-17T01:00:00Z","updatedAt":"2026-11-17T01:20:00Z","deleted":false}""",
            )

            // when
            source.apply(listOf(record))

            // then
            database.ideaDao().observeAll().first().map { it.text } shouldContainExactlyInAnyOrder
                listOf("my-text-1", "my-text-2")
        }
    }
}
