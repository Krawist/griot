package com.griot.core.data.song

import com.griot.core.data.TestClock
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.database.entity.VersionEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.NewSection
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongSnapshot
import com.griot.core.data.model.SongStatus
import com.griot.core.data.model.VersionSource
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// On an in-memory database, with the real editor repository: versions are checked as they are read back.
internal class VersionRepositoryImplTest {
    private val database = inMemoryDatabase()
    private val clock = TestClock(Instant.parse("2026-11-16T10:00:00Z"))
    private val editor = SongEditorRepositoryImpl(
        database = database,
        songDao = database.songDao(),
        sectionDao = database.sectionDao(),
        verseDao = database.verseDao(),
        clock = clock,
    )
    private val repository = VersionRepositoryImpl(
        versionDao = database.versionDao(),
        sectionDao = database.sectionDao(),
        verseDao = database.verseDao(),
        editor = editor,
        clock = clock,
    )

    @BeforeEach
    fun setUp() = runTest {
        // Every test works on one song: a verse section holding one verse.
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
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class RecordVerse {
        @Test
        fun `should record the verse once while its text does not change`() = runTest {
            // when
            repository.recordVerse("my-verse-id")
            repository.recordVerse("my-verse-id")

            // then
            repository.observeVerseHistory("my-verse-id").first().map { it.text } shouldContainExactly
                listOf("my-text-1")
        }
    }

    @Nested
    inner class RecordSong {
        @Test
        fun `should wait 5 minutes before recording the song again`() = runTest {
            // given
            repository.recordSong("my-song-id")
            editor.updateVerse(verseId = "my-verse-id", text = "my-text-2")
            clock.now = Instant.parse("2026-11-16T10:04:00Z")
            repository.recordSong("my-song-id")

            // when
            clock.now = Instant.parse("2026-11-16T10:05:00Z")
            repository.recordSong("my-song-id")

            // then
            repository.observeSongHistory("my-song-id").first().map { version ->
                version.snapshot.sections.single().lines
            } shouldContainExactly listOf(listOf("my-text-2"), listOf("my-text-1"))
        }

        @Test
        fun `should not record a song that did not change`() = runTest {
            // given
            repository.recordSong("my-song-id")

            // when
            clock.now = Instant.parse("2026-11-16T11:00:00Z")
            repository.recordSong("my-song-id")

            // then
            repository.observeSongHistory("my-song-id").first().size shouldBe 1
        }
    }

    @Nested
    inner class RestoreVerse {
        @Test
        fun `should keep the current text, then restore the old one as a new version`() = runTest {
            // given
            repository.recordVerse("my-verse-id")
            editor.updateVerse(verseId = "my-verse-id", text = "my-text-2")
            clock.now = Instant.parse("2026-11-16T10:01:00Z")
            val old = repository.observeVerseHistory("my-verse-id").first().single()

            // when
            repository.restoreVerse(old.id)

            // then
            database.verseDao().getById("my-verse-id")?.text shouldBe "my-text-1"
            repository.observeVerseHistory("my-verse-id").first().map { it.text to it.source } shouldContainExactly
                listOf(
                    "my-text-1" to VersionSource.RESTORE,
                    "my-text-2" to VersionSource.AUTHOR,
                    "my-text-1" to VersionSource.AUTHOR,
                )
        }
    }

    @Nested
    inner class RestoreSong {
        @Test
        fun `should give the song the content of the version and keep the one before`() = runTest {
            // given
            repository.recordSong("my-song-id")
            editor.replaceContent(
                songId = "my-song-id",
                sections = listOf(
                    NewSection(kind = SectionKind.CHORUS, name = null, lines = listOf("a"), repeatOf = null),
                ),
            )
            val old = repository.observeSongHistory("my-song-id").first().single()

            // when
            repository.restoreSong(old.id)

            // then
            editor.observeDocument("my-song-id").first()?.sections?.single()?.verses?.map { it.text } shouldBe
                listOf("my-text-1")
            repository.observeSongHistory("my-song-id").first().map { it.source } shouldContainExactly listOf(
                VersionSource.RESTORE,
                VersionSource.AUTHOR,
                VersionSource.AUTHOR,
            )
        }
    }

    @Nested
    inner class Thin {
        @Test
        fun `should keep the last version of each old day, and every recent one`() = runTest {
            // given
            listOf(
                "my-version-id-1" to "2026-11-01T09:00:00Z",
                "my-version-id-2" to "2026-11-01T18:00:00Z",
                "my-version-id-3" to "2026-11-02T09:00:00Z",
                "my-version-id-4" to "2026-11-15T09:00:00Z",
                "my-version-id-5" to "2026-11-15T10:00:00Z",
            ).forEach { (id, at) ->
                database.versionDao().insert(
                    VersionEntity(
                        id = id,
                        songId = "my-song-id",
                        verseId = "my-verse-id",
                        text = id,
                        source = VersionSource.AUTHOR,
                        createdAt = Instant.parse(at),
                        updatedAt = Instant.parse(at),
                    ),
                )
            }

            // when
            repository.thin()

            // then
            repository.observeVerseHistory("my-verse-id").first().map { it.id } shouldContainExactly listOf(
                "my-version-id-5",
                "my-version-id-4",
                "my-version-id-3",
                "my-version-id-2",
            )
        }
    }

    @Nested
    inner class Snapshot {
        @Test
        fun `should keep a repeat pointing to its source in a song version`() = runTest {
            // given
            editor.replaceContent(
                songId = "my-song-id",
                sections = listOf(
                    NewSection(kind = SectionKind.CHORUS, name = null, lines = listOf("a"), repeatOf = null),
                    NewSection(kind = SectionKind.CHORUS, name = null, lines = emptyList(), repeatOf = 0),
                ),
            )

            // when
            repository.recordSong("my-song-id")

            // then
            repository.observeSongHistory("my-song-id").first().single().snapshot shouldBe SongSnapshot(
                sections = listOf(
                    NewSection(kind = SectionKind.CHORUS, name = null, lines = listOf("a"), repeatOf = null),
                    NewSection(kind = SectionKind.CHORUS, name = null, lines = emptyList(), repeatOf = 0),
                ),
            )
        }
    }
}
