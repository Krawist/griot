package com.griot.core.data.song

import com.griot.core.data.database.dao.LibrarySongRow
import com.griot.core.data.database.dao.SongDao
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SongRepositoryImplTest {
    private val songDao = mockk<SongDao>()
    private val preferences = mockk<UserPreferencesRepository>()
    private val clock = Clock.fixed(Instant.parse("2026-11-16T10:00:00Z"), ZoneOffset.UTC)
    private val repository = SongRepositoryImpl(songDao = songDao, preferences = preferences, clock = clock)

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
                ),
            )
            verifySequence { songDao.observeLibrary() }
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

            // when
            val library = repository.observeLibrary().first()

            // then
            library.single().lyrics shouldBe ""
            verifySequence { songDao.observeLibrary() }
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
}
