package com.griot.core.data.database.dao

import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class VerseDaoTest {
    private lateinit var database: GriotDatabase
    private lateinit var songDao: SongDao
    private lateinit var sectionDao: SectionDao
    private lateinit var verseDao: VerseDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        songDao = database.songDao()
        sectionDao = database.sectionDao()
        verseDao = database.verseDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class ObserveBySection {
        @Test
        fun `should list the verses of the section in order`() = runTest {
            // given
            songDao.insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            sectionDao.insert(
                SectionEntity(
                    id = "my-section-id",
                    songId = "my-song-id",
                    kind = SectionKind.VERSE,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            val second = VerseEntity(
                id = "my-verse-id-1",
                sectionId = "my-section-id",
                text = "my-text-1",
                position = 1,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            val first = VerseEntity(
                id = "my-verse-id-2",
                sectionId = "my-section-id",
                text = "my-text-2",
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            verseDao.insert(second)
            verseDao.insert(first)

            // when
            val verses = verseDao.observeBySection("my-section-id").first()

            // then
            verses shouldContainExactly listOf(first, second)
        }

        @Test
        fun `should show the verses of the source in a repeated chorus`() = runTest {
            // given
            songDao.insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            sectionDao.insert(
                SectionEntity(
                    id = "my-section-id-1",
                    songId = "my-song-id",
                    kind = SectionKind.CHORUS,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            sectionDao.insert(
                SectionEntity(
                    id = "my-section-id-2",
                    songId = "my-song-id",
                    kind = SectionKind.CHORUS,
                    position = 1,
                    repeatOfSectionId = "my-section-id-1",
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            val verse = VerseEntity(
                id = "my-verse-id",
                sectionId = "my-section-id-1",
                text = "my-text",
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            verseDao.insert(verse)

            // when
            val verses = verseDao.observeBySection("my-section-id-2").first()

            // then
            verses shouldContainExactly listOf(verse)
        }

        @Test
        fun `should show an edit of the source in a repeated chorus`() = runTest {
            // given
            songDao.insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            sectionDao.insert(
                SectionEntity(
                    id = "my-section-id-1",
                    songId = "my-song-id",
                    kind = SectionKind.CHORUS,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            sectionDao.insert(
                SectionEntity(
                    id = "my-section-id-2",
                    songId = "my-song-id",
                    kind = SectionKind.CHORUS,
                    position = 1,
                    repeatOfSectionId = "my-section-id-1",
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            val verse = VerseEntity(
                id = "my-verse-id",
                sectionId = "my-section-id-1",
                text = "my-text-1",
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            verseDao.insert(verse)
            val edited = verse.copy(text = "my-text-2", updatedAt = Instant.parse("2026-11-16T11:00:00Z"))

            // when
            verseDao.update(edited)

            // then
            verseDao.observeBySection("my-section-id-2").first() shouldContainExactly listOf(edited)
        }

        @Test
        fun `should leave out the deleted verses`() = runTest {
            // given
            songDao.insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            sectionDao.insert(
                SectionEntity(
                    id = "my-section-id",
                    songId = "my-song-id",
                    kind = SectionKind.VERSE,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            val kept = VerseEntity(
                id = "my-verse-id-1",
                sectionId = "my-section-id",
                text = "my-text-1",
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            verseDao.insert(kept)
            verseDao.insert(
                VerseEntity(
                    id = "my-verse-id-2",
                    sectionId = "my-section-id",
                    text = "my-text-2",
                    position = 1,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    deleted = true,
                ),
            )

            // when
            val verses = verseDao.observeBySection("my-section-id").first()

            // then
            verses shouldContainExactly listOf(kept)
        }
    }

    @Nested
    inner class MarkDeleted {
        @Test
        fun `should keep the row flagged as deleted with the time of the delete`() = runTest {
            // given
            songDao.insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            sectionDao.insert(
                SectionEntity(
                    id = "my-section-id",
                    songId = "my-song-id",
                    kind = SectionKind.VERSE,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            val verse = VerseEntity(
                id = "my-verse-id",
                sectionId = "my-section-id",
                text = "my-text",
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            verseDao.insert(verse)

            // when
            verseDao.markDeleted(id = "my-verse-id", at = Instant.parse("2026-11-16T12:00:00Z"))

            // then
            verseDao.getById("my-verse-id") shouldBe verse.copy(
                deleted = true,
                updatedAt = Instant.parse("2026-11-16T12:00:00Z"),
            )
        }
    }
}
