package com.griot.core.data.song

import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.SongStatus
import com.griot.core.data.model.SongVerse
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// On an in-memory database: every change runs in a transaction, and is checked as it is read back.
internal class SongEditorRepositoryImplTest {
    private val database = inMemoryDatabase()
    private val clock = Clock.fixed(Instant.parse("2026-11-16T10:00:00Z"), ZoneOffset.UTC)
    private val repository = SongEditorRepositoryImpl(
        database = database,
        songDao = database.songDao(),
        sectionDao = database.sectionDao(),
        verseDao = database.verseDao(),
        clock = clock,
    )

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class ObserveDocument {
        @Test
        fun `should give the sections in order, a repeat showing the verses of its source`() = runTest {
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
            val document = repository.observeDocument("my-song-id").first()

            // then
            document.shouldNotBeNull().sections shouldContainExactly listOf(
                SongSection(
                    id = "my-section-id-1",
                    kind = SectionKind.CHORUS,
                    name = null,
                    repeatOf = null,
                    verses = listOf(SongVerse(id = "my-verse-id", text = "my-text")),
                ),
                SongSection(
                    id = "my-section-id-2",
                    kind = SectionKind.CHORUS,
                    name = null,
                    repeatOf = "my-section-id-1",
                    verses = listOf(SongVerse(id = "my-verse-id", text = "my-text")),
                ),
            )
        }

        @Test
        fun `should give the title and the language of the song`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.ENGLISH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-01T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )

            // when
            val document = repository.observeDocument("my-song-id").first()

            // then
            document shouldBe SongDocument(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.ENGLISH,
                sections = emptyList(),
            )
        }

        @Test
        fun `should give nothing for a song in the trash`() = runTest {
            // given
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-01T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    deleted = true,
                ),
            )

            // when
            val document = repository.observeDocument("my-song-id").first()

            // then
            document.shouldBeNull()
        }
    }

    @Nested
    inner class AddSection {
        @Test
        fun `should add the section at the end with an empty verse and date the song`() = runTest {
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
                        id = "my-section-id",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                ),
                verses = emptyList(),
            )

            // when
            val id = repository.addSection(songId = "my-song-id", kind = SectionKind.BRIDGE)

            // then
            val sections = repository.observeDocument("my-song-id").first().shouldNotBeNull().sections
            sections.map { it.id } shouldContainExactly listOf("my-section-id", id)
            sections[1].kind shouldBe SectionKind.BRIDGE
            sections[1].verses.map { it.text } shouldContainExactly listOf("")
            database.songDao().getById("my-song-id")?.updatedAt shouldBe Instant.parse("2026-11-16T10:00:00Z")
        }
    }

    @Nested
    inner class RepeatSection {
        @Test
        fun `should repeat the source of a repeat`() = runTest {
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
                verses = emptyList(),
            )

            // when
            val id = repository.repeatSection("my-section-id-2")

            // then
            database.sectionDao().getById(id) shouldBe SectionEntity(
                id = id,
                songId = "my-song-id",
                kind = SectionKind.CHORUS,
                position = 2,
                repeatOfSectionId = "my-section-id-1",
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
        }
    }

    @Nested
    inner class RenameSection {
        @Test
        fun `should give back the automatic label for a blank name`() = runTest {
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
                        id = "my-section-id",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        name = "my-name",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                ),
                verses = emptyList(),
            )

            // when
            repository.renameSection(sectionId = "my-section-id", name = "  ")

            // then
            database.sectionDao().getById("my-section-id")?.name.shouldBeNull()
        }

        @Test
        fun `should save the name without the spaces around it`() = runTest {
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
                        id = "my-section-id",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                ),
                verses = emptyList(),
            )

            // when
            repository.renameSection(sectionId = "my-section-id", name = " my-name ")

            // then
            database.sectionDao().getById("my-section-id")?.name shouldBe "my-name"
        }
    }

    @Nested
    inner class MoveSection {
        @Test
        fun `should move the section up`() = runTest {
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
                        kind = SectionKind.VERSE,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                    SectionEntity(
                        id = "my-section-id-2",
                        songId = "my-song-id",
                        kind = SectionKind.CHORUS,
                        position = 1,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                ),
                verses = emptyList(),
            )

            // when
            repository.moveSection(sectionId = "my-section-id-2", offset = -1)

            // then
            repository.observeDocument("my-song-id").first().shouldNotBeNull().sections.map {
                it.id
            } shouldContainExactly
                listOf("my-section-id-2", "my-section-id-1")
        }

        @Test
        fun `should leave the first section in place when moved up`() = runTest {
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
                        id = "my-section-id",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                ),
                verses = emptyList(),
            )

            // when
            repository.moveSection(sectionId = "my-section-id", offset = -1)

            // then
            database.songDao().getById("my-song-id")?.updatedAt shouldBe Instant.parse("2026-11-01T10:00:00Z")
        }
    }

    @Nested
    inner class DeleteSection {
        @Test
        fun `should delete the section with its repeats`() = runTest {
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
                        kind = SectionKind.VERSE,
                        position = 1,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                    SectionEntity(
                        id = "my-section-id-3",
                        songId = "my-song-id",
                        kind = SectionKind.CHORUS,
                        position = 2,
                        repeatOfSectionId = "my-section-id-1",
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                ),
                verses = emptyList(),
            )

            // when
            repository.deleteSection("my-section-id-1")

            // then
            repository.observeDocument("my-song-id").first().shouldNotBeNull().sections.map {
                it.id
            } shouldContainExactly
                listOf("my-section-id-2")
        }
    }

    @Nested
    inner class UpdateVerse {
        @Test
        fun `should save the text and date the song`() = runTest {
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
                        id = "my-section-id",
                        songId = "my-song-id",
                        kind = SectionKind.VERSE,
                        position = 0,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                ),
                verses = listOf(
                    VerseEntity(
                        id = "my-verse-id",
                        sectionId = "my-section-id",
                        text = "my-text-1",
                        position = 0,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                ),
            )

            // when
            repository.updateVerse(verseId = "my-verse-id", text = "my-text-2")

            // then
            database.verseDao().getById("my-verse-id")?.text shouldBe "my-text-2"
            database.songDao().getById("my-song-id")?.updatedAt shouldBe Instant.parse("2026-11-16T10:00:00Z")
        }
    }
}
