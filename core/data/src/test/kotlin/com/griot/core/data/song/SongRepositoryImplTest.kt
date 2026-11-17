package com.griot.core.data.song

import com.griot.core.data.database.dao.FirstSectionVerseRow
import com.griot.core.data.database.dao.LibrarySongRow
import com.griot.core.data.database.dao.SectionDao
import com.griot.core.data.database.dao.SongDao
import com.griot.core.data.database.dao.TrashedSongRow
import com.griot.core.data.database.dao.VerseDao
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.NewSection
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.data.model.TrashedSong
import com.griot.core.data.preferences.UserPreferencesRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coJustRun
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verifySequence
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SongRepositoryImplTest {
    private val songDao = mockk<SongDao>()
    private val sectionDao = mockk<SectionDao>()
    private val verseDao = mockk<VerseDao>()
    private val preferences = mockk<UserPreferencesRepository>()
    private val clock = Clock.fixed(Instant.parse("2026-11-16T10:00:00Z"), ZoneOffset.UTC)
    private val repository = SongRepositoryImpl(
        songDao = songDao,
        sectionDao = sectionDao,
        verseDao = verseDao,
        preferences = preferences,
        clock = clock,
    )

    @Nested
    inner class ObserveLibrary {
        @Test
        fun `should list the songs of the database`() = runTest {
            // given
            every { songDao.observeLibrary() } returns flowOf(
                listOf(
                    LibrarySongRow(
                        id = "my-song-id",
                        title = "my-title",
                        language = SongLanguage.ENGLISH,
                        status = SongStatus.IN_PROGRESS,
                        updatedAt = Instant.parse("2026-11-16T09:00:00Z"),
                        sectionCount = 3,
                        firstVerse = "my-first-verse",
                        lyrics = "my-lyrics",
                    ),
                ),
            )
            every { songDao.observeFirstSections() } returns flowOf(
                listOf(
                    FirstSectionVerseRow(songId = "my-song-id", text = "my-verse-1"),
                    FirstSectionVerseRow(songId = "my-song-id", text = "my-verse-2"),
                ),
            )

            // when
            val library = repository.observeLibrary().first()

            // then
            library shouldContainExactly listOf(
                LibrarySong(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.ENGLISH,
                    status = SongStatus.IN_PROGRESS,
                    updatedAt = Instant.parse("2026-11-16T09:00:00Z"),
                    sectionCount = 3,
                    firstVerse = "my-first-verse",
                    lyrics = "my-lyrics",
                    firstSection = listOf("my-verse-1", "my-verse-2"),
                ),
            )
            verifySequence {
                songDao.observeLibrary()
                songDao.observeFirstSections()
            }
        }

        @Test
        fun `should give empty lyrics to a song without verses`() = runTest {
            // given
            every { songDao.observeLibrary() } returns flowOf(
                listOf(
                    LibrarySongRow(
                        id = "my-song-id",
                        title = "",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        updatedAt = Instant.parse("2026-11-16T09:00:00Z"),
                        sectionCount = 0,
                        firstVerse = null,
                        lyrics = null,
                    ),
                ),
            )
            every { songDao.observeFirstSections() } returns flowOf(emptyList())

            // when
            val library = repository.observeLibrary().first()

            // then
            library.single().lyrics shouldBe ""
            verifySequence {
                songDao.observeLibrary()
                songDao.observeFirstSections()
            }
        }
    }

    @Nested
    inner class CreateSong {
        @Test
        fun `should create an empty draft with a verse section and an empty verse`() = runTest {
            // given
            every { preferences.defaultSongLanguage } returns flowOf(SongLanguage.ENGLISH)
            val song = slot<SongEntity>()
            val sections = slot<List<SectionEntity>>()
            val verses = slot<List<VerseEntity>>()
            coJustRun { songDao.insertWithContent(capture(song), capture(sections), capture(verses)) }

            // when
            val id = repository.createSong()

            // then
            // The ids are random UUIDs: each row is checked against the others.
            song.captured shouldBe SongEntity(
                id = id,
                title = "",
                language = SongLanguage.ENGLISH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            val section = sections.captured.single()
            section shouldBe SectionEntity(
                id = section.id,
                songId = id,
                kind = SectionKind.VERSE,
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            verses.captured shouldContainExactly listOf(
                VerseEntity(
                    id = verses.captured.single().id,
                    sectionId = section.id,
                    text = "",
                    position = 0,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            setOf(id, section.id, verses.captured.single().id).shouldHaveSize(3)
            coVerifySequence {
                preferences.defaultSongLanguage
                songDao.insertWithContent(song.captured, sections.captured, verses.captured)
            }
        }
    }

    @Nested
    inner class ObserveTrash {
        @Test
        fun `should list the songs in the trash`() = runTest {
            // given
            every { songDao.observeTrash() } returns flowOf(
                listOf(
                    TrashedSongRow(
                        id = "my-song-id",
                        title = "my-title",
                        deletedAt = Instant.parse("2026-11-15T10:00:00Z"),
                    ),
                ),
            )

            // when
            val trash = repository.observeTrash().first()

            // then
            trash shouldContainExactly listOf(
                TrashedSong(id = "my-song-id", title = "my-title", deletedAt = Instant.parse("2026-11-15T10:00:00Z")),
            )
            verifySequence { songDao.observeTrash() }
        }
    }

    @Nested
    inner class RenameSong {
        @Test
        fun `should rename the song now`() = runTest {
            // given
            coJustRun { songDao.rename(any(), any(), any()) }

            // when
            repository.renameSong(id = "my-song-id", title = "my-title")

            // then
            coVerifySequence {
                songDao.rename(id = "my-song-id", title = "my-title", at = Instant.parse("2026-11-16T10:00:00Z"))
            }
        }
    }

    @Nested
    inner class DeleteSong {
        @Test
        fun `should move the song to the trash now`() = runTest {
            // given
            coJustRun { songDao.markDeleted(any(), any()) }

            // when
            repository.deleteSong("my-song-id")

            // then
            coVerifySequence { songDao.markDeleted(id = "my-song-id", at = Instant.parse("2026-11-16T10:00:00Z")) }
        }
    }

    @Nested
    inner class RestoreSong {
        @Test
        fun `should take the song out of the trash now`() = runTest {
            // given
            coJustRun { songDao.restore(any(), any()) }

            // when
            repository.restoreSong("my-song-id")

            // then
            coVerifySequence { songDao.restore(id = "my-song-id", at = Instant.parse("2026-11-16T10:00:00Z")) }
        }
    }

    @Nested
    inner class PurgeTrash {
        @Test
        fun `should remove the songs deleted more than 30 days ago`() = runTest {
            // given
            coJustRun { songDao.purgeDeletedBefore(any()) }

            // when
            repository.purgeTrash()

            // then
            coVerifySequence { songDao.purgeDeletedBefore(Instant.parse("2026-10-17T10:00:00Z")) }
        }
    }

    // On an in-memory database: the copy is checked row by row, as it is read back.
    @Nested
    inner class DuplicateSong {
        private val database = inMemoryDatabase()
        private val repository = SongRepositoryImpl(
            songDao = database.songDao(),
            sectionDao = database.sectionDao(),
            verseDao = database.verseDao(),
            preferences = preferences,
            clock = clock,
        )

        @AfterEach
        fun tearDown() {
            database.close()
        }

        @Test
        fun `should copy the song under the new title`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title-1",
                    language = SongLanguage.ENGLISH,
                    status = SongStatus.IN_PROGRESS,
                    createdAt = Instant.parse("2026-11-01T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-02T10:00:00Z"),
                ),
            )

            // when
            val copyId = repository.duplicateSong(id = "my-song-id", title = "my-title-2")

            // then
            database.songDao().getById(copyId) shouldBe SongEntity(
                id = copyId,
                title = "my-title-2",
                language = SongLanguage.ENGLISH,
                status = SongStatus.IN_PROGRESS,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
        }

        @Test
        fun `should copy the verses and point a repeat to the copied section`() = runTest {
            // given
            database.songDao().insertWithContent(
                song = SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-01T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
                sections = listOf(
                    SectionEntity(
                        id = "my-section-id-1",
                        songId = "my-song-id",
                        kind = SectionKind.CHORUS,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                    SectionEntity(
                        id = "my-section-id-2",
                        songId = "my-song-id",
                        kind = SectionKind.CHORUS,
                        position = 1,
                        repeatOfSectionId = "my-section-id-1",
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                ),
                verses = listOf(
                    VerseEntity(
                        id = "my-verse-id",
                        sectionId = "my-section-id-1",
                        text = "my-text",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                ),
            )

            // when
            val copyId = repository.duplicateSong(id = "my-song-id", title = "my-title")

            // then
            val sections = database.sectionDao().getBySong(copyId)
            sections.map { it.kind to it.position } shouldContainExactly
                listOf(SectionKind.CHORUS to 0, SectionKind.CHORUS to 1)
            sections[1].repeatOfSectionId shouldBe sections[0].id
            database.verseDao().observeBySection(sections[1].id).first().map { it.text } shouldContainExactly
                listOf("my-text")
            database.verseDao().getBySong("my-song-id").single().id shouldBe "my-verse-id"
        }
    }

    // On an in-memory database: the song is checked as it is read back.
    @Nested
    inner class ImportSong {
        private val database = inMemoryDatabase()
        private val repository = SongRepositoryImpl(
            songDao = database.songDao(),
            sectionDao = database.sectionDao(),
            verseDao = database.verseDao(),
            preferences = preferences,
            clock = clock,
        )

        @AfterEach
        fun tearDown() {
            database.close()
        }

        @Test
        fun `should create a song in progress, in automatic language, with its sections and repeats`() = runTest {
            // when
            val id = repository.importSong(
                title = "my-title",
                sections = listOf(
                    NewSection(kind = SectionKind.CHORUS, name = null, lines = listOf("a", "b"), repeatOf = null),
                    NewSection(kind = SectionKind.OTHER, name = "my-name", lines = listOf("c"), repeatOf = null),
                    NewSection(kind = SectionKind.CHORUS, name = null, lines = emptyList(), repeatOf = 0),
                ),
            )

            // then
            database.songDao().getById(id)?.let { Triple(it.title, it.status, it.language) } shouldBe
                Triple("my-title", SongStatus.IN_PROGRESS, SongLanguage.AUTOMATIC)
            val sections = database.sectionDao().getBySong(id)
            sections.map { it.kind to it.name } shouldContainExactly listOf(
                SectionKind.CHORUS to null,
                SectionKind.OTHER to "my-name",
                SectionKind.CHORUS to null,
            )
            sections[2].repeatOfSectionId shouldBe sections[0].id
            database.verseDao().getBySong(id).map { it.text } shouldContainExactly listOf("a", "b", "c")
        }
    }
}
