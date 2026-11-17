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
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SongDaoTest {
    private lateinit var database: GriotDatabase
    private lateinit var songDao: SongDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        songDao = database.songDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class GetById {
        @Test
        fun `should find a song by its id`() = runTest {
            // given
            val song = SongEntity(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:05:00Z"),
            )
            songDao.insert(song)

            // when
            val found = songDao.getById("my-song-id")

            // then
            found shouldBe song
        }

        @Test
        fun `should return null for an unknown song`() = runTest {
            // when
            val found = songDao.getById("my-song-id")

            // then
            found.shouldBeNull()
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `should save the new title and change time`() = runTest {
            // given
            val song = SongEntity(
                id = "my-song-id",
                title = "my-title-1",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            songDao.insert(song)
            val renamed = song.copy(title = "my-title-2", updatedAt = Instant.parse("2026-11-16T11:00:00Z"))

            // when
            songDao.update(renamed)

            // then
            songDao.getById("my-song-id") shouldBe renamed
        }
    }

    @Nested
    inner class ObserveAll {
        @Test
        fun `should list the songs last changed first`() = runTest {
            // given
            val older = SongEntity(
                id = "my-song-id-1",
                title = "my-title-1",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-01T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-15T10:00:00Z"),
            )
            val newer = SongEntity(
                id = "my-song-id-2",
                title = "my-title-2",
                language = SongLanguage.ENGLISH,
                status = SongStatus.IN_PROGRESS,
                createdAt = Instant.parse("2026-11-02T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            songDao.insert(older)
            songDao.insert(newer)

            // when
            val songs = songDao.observeAll().first()

            // then
            songs shouldContainExactly listOf(newer, older)
        }

        @Test
        fun `should leave out the songs in the trash`() = runTest {
            // given
            val kept = SongEntity(
                id = "my-song-id-1",
                title = "my-title-1",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            val deleted = SongEntity(
                id = "my-song-id-2",
                title = "my-title-2",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T11:00:00Z"),
                deleted = true,
            )
            songDao.insert(kept)
            songDao.insert(deleted)

            // when
            val songs = songDao.observeAll().first()

            // then
            songs shouldContainExactly listOf(kept)
        }
    }

    @Nested
    inner class MarkDeleted {
        @Test
        fun `should keep the row flagged as deleted with the time of the delete`() = runTest {
            // given
            val song = SongEntity(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            songDao.insert(song)

            // when
            songDao.markDeleted(id = "my-song-id", at = Instant.parse("2026-11-16T12:00:00Z"))

            // then
            songDao.getById("my-song-id") shouldBe song.copy(
                deleted = true,
                updatedAt = Instant.parse("2026-11-16T12:00:00Z"),
            )
        }
    }

    @Nested
    inner class InsertWithContent {
        @Test
        fun `should insert the song with its sections and verses`() = runTest {
            // given
            val song = SongEntity(
                id = "my-song-id",
                title = "",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            val section = SectionEntity(
                id = "my-section-id",
                songId = "my-song-id",
                kind = SectionKind.VERSE,
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            val verse = VerseEntity(
                id = "my-verse-id",
                sectionId = "my-section-id",
                text = "",
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )

            // when
            songDao.insertWithContent(song = song, sections = listOf(section), verses = listOf(verse))

            // then
            songDao.getById("my-song-id") shouldBe song
            database.sectionDao().getById("my-section-id") shouldBe section
            database.verseDao().getById("my-verse-id") shouldBe verse
        }
    }

    @Nested
    inner class ObserveLibrary {
        @Test
        fun `should list each song with its section count and first verse`() = runTest {
            // given
            songDao.insertWithContent(
                song = SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.IN_PROGRESS,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T11:00:00Z"),
                ),
                sections = listOf(
                    SectionEntity(
                        id = "my-section-id-1",
                        songId = "my-song-id",
                        kind = SectionKind.CHORUS,
                        position = 1,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                    SectionEntity(
                        id = "my-section-id-2",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
                verses = listOf(
                    VerseEntity(
                        id = "my-verse-id-1",
                        sectionId = "my-section-id-1",
                        text = "my-text-1",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                    VerseEntity(
                        id = "my-verse-id-2",
                        sectionId = "my-section-id-2",
                        text = "my-text-2",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
            )

            // when
            val library = songDao.observeLibrary().first()

            // then
            library.single().shouldBeEqualToIgnoringFields(
                LibrarySongRow(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.IN_PROGRESS,
                    updatedAt = Instant.parse("2026-11-16T11:00:00Z"),
                    sectionCount = 2,
                    firstVerse = "my-text-2",
                    lyrics = null,
                ),
                LibrarySongRow::lyrics,
            )
        }

        @Test
        fun `should list each song with all its verses as lyrics`() = runTest {
            // given
            songDao.insertWithContent(
                song = SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.IN_PROGRESS,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T11:00:00Z"),
                ),
                sections = listOf(
                    SectionEntity(
                        id = "my-section-id-1",
                        songId = "my-song-id",
                        kind = SectionKind.CHORUS,
                        position = 1,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                    SectionEntity(
                        id = "my-section-id-2",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
                verses = listOf(
                    VerseEntity(
                        id = "my-verse-id-1",
                        sectionId = "my-section-id-1",
                        text = "my-text-1",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                    VerseEntity(
                        id = "my-verse-id-2",
                        sectionId = "my-section-id-2",
                        text = "my-text-2",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
            )

            // when
            val library = songDao.observeLibrary().first()

            // then
            library.single().lyrics.shouldNotBeNull().lines() shouldContainExactlyInAnyOrder
                listOf("my-text-1", "my-text-2")
        }

        @Test
        fun `should skip the empty verses for the first verse`() = runTest {
            // given
            songDao.insertWithContent(
                song = SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
                sections = listOf(
                    SectionEntity(
                        id = "my-section-id",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
                verses = listOf(
                    VerseEntity(
                        id = "my-verse-id-1",
                        sectionId = "my-section-id",
                        text = " ",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                    VerseEntity(
                        id = "my-verse-id-2",
                        sectionId = "my-section-id",
                        text = "my-text",
                        position = 1,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
            )

            // when
            val library = songDao.observeLibrary().first()

            // then
            library.single().firstVerse shouldBe "my-text"
        }

        @Test
        fun `should leave out the deleted sections and verses`() = runTest {
            // given
            songDao.insertWithContent(
                song = SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
                sections = listOf(
                    SectionEntity(
                        id = "my-section-id-1",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                        deleted = true,
                    ),
                    SectionEntity(
                        id = "my-section-id-2",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        position = 1,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
                verses = listOf(
                    VerseEntity(
                        id = "my-verse-id-1",
                        sectionId = "my-section-id-1",
                        text = "my-text-1",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                    VerseEntity(
                        id = "my-verse-id-2",
                        sectionId = "my-section-id-2",
                        text = "my-text-2",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                        deleted = true,
                    ),
                ),
            )

            // when
            val library = songDao.observeLibrary().first()

            // then
            library.single() shouldBe LibrarySongRow(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                sectionCount = 1,
                firstVerse = null,
                lyrics = null,
            )
        }
    }

    @Nested
    inner class ObserveFirstSections {
        @Test
        fun `should list the verses of the first section of each song, in order`() = runTest {
            // given
            songDao.insertWithContent(
                song = SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.IN_PROGRESS,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
                sections = listOf(
                    SectionEntity(
                        id = "my-section-id-1",
                        songId = "my-song-id",
                        kind = SectionKind.CHORUS,
                        position = 1,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                    SectionEntity(
                        id = "my-section-id-2",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
                verses = listOf(
                    VerseEntity(
                        id = "my-verse-id-1",
                        sectionId = "my-section-id-1",
                        text = "my-text-1",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                    VerseEntity(
                        id = "my-verse-id-2",
                        sectionId = "my-section-id-2",
                        text = "my-text-2",
                        position = 1,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                    VerseEntity(
                        id = "my-verse-id-3",
                        sectionId = "my-section-id-2",
                        text = "my-text-3",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
            )

            // when
            val verses = songDao.observeFirstSections().first()

            // then
            verses shouldContainExactly listOf(
                FirstSectionVerseRow(songId = "my-song-id", text = "my-text-3"),
                FirstSectionVerseRow(songId = "my-song-id", text = "my-text-2"),
            )
        }

        @Test
        fun `should give the verses of its source to a first section that repeats another`() = runTest {
            // given
            songDao.insertWithContent(
                song = SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.IN_PROGRESS,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
                sections = listOf(
                    SectionEntity(
                        id = "my-section-id-1",
                        songId = "my-song-id",
                        kind = SectionKind.CHORUS,
                        position = 1,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                    SectionEntity(
                        id = "my-section-id-2",
                        songId = "my-song-id",
                        kind = SectionKind.CHORUS,
                        position = 0,
                        repeatOfSectionId = "my-section-id-1",
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
                verses = listOf(
                    VerseEntity(
                        id = "my-verse-id",
                        sectionId = "my-section-id-1",
                        text = "my-text",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
            )

            // when
            val verses = songDao.observeFirstSections().first()

            // then
            verses shouldContainExactly listOf(FirstSectionVerseRow(songId = "my-song-id", text = "my-text"))
        }
    }
}
