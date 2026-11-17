package com.griot.core.data.tidy

import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.SongFolderEntity
import com.griot.core.data.database.entity.VersionEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.data.model.TidyUpKind
import com.griot.core.data.model.VersionSource
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import com.griot.core.data.song.FakeSongRepository
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

// On an in-memory database: refusals and merges are read back.
internal class TidyUpRepositoryImplTest {
    private val database = inMemoryDatabase()
    private val clock = Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC)

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class ObserveDue {
        @Test
        fun `should be due after an import`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            preferences.setTidyUpReviewed(Instant.parse("2026-11-16T10:00:00Z"))
            preferences.requestTidyUp()
            val repository = TidyUpRepositoryImpl(database, FakeSongRepository(), preferences, clock)

            // when
            val due = repository.observeDue().first()

            // then
            due shouldBe true
        }

        @Test
        fun `should wait a week after the proposals were looked at`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            preferences.setTidyUpReviewed(Instant.parse("2026-11-11T10:00:00Z"))
            val repository = TidyUpRepositoryImpl(database, FakeSongRepository(), preferences, clock)

            // when
            val due = repository.observeDue().first()

            // then
            due shouldBe false
        }
    }

    @Nested
    inner class ObserveCandidates {
        @Test
        fun `should leave out what the author refused`() = runTest {
            // given
            val songs = listOf("my-song-id-1", "my-song-id-2").map {
                LibrarySong(
                    id = it,
                    title = "",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.IN_PROGRESS,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "my-lyrics",
                )
            }
            val repository =
                TidyUpRepositoryImpl(
                    database,
                    FakeSongRepository(library = songs),
                    FakeUserPreferencesRepository(),
                    clock,
                )
            repository.refuse(kind = TidyUpKind.TITLE, keys = listOf("my-song-id-1"))
            repository.refuse(kind = TidyUpKind.DUPLICATE, keys = listOf("my-song-id-1,my-song-id-2"))

            // when
            val candidates = repository.observeCandidates().first()

            // then
            candidates.untitled.map { it.id } shouldContainExactly listOf("my-song-id-2")
            candidates.duplicates.shouldBeEmpty()
        }
    }

    @Nested
    inner class MergeDuplicates {
        @Test
        fun `should give the duplicate's versions and folders to the song kept and trash it`() = runTest {
            // given
            listOf("my-song-id-1", "my-song-id-2").forEach {
                database.songDao().insert(
                    SongEntity(
                        id = it,
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.IN_PROGRESS,
                        createdAt = Instant.parse("2026-11-01T10:00:00Z"),
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                )
            }
            database.versionDao().insert(
                VersionEntity(
                    id = "my-version-id",
                    songId = "my-song-id-2",
                    verseId = null,
                    text = "my-text",
                    source = VersionSource.AUTHOR,
                    createdAt = Instant.parse("2026-11-01T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )
            database.songFolderDao().insert(
                SongFolderEntity(
                    id = "my-song-id-2:my-folder-id",
                    songId = "my-song-id-2",
                    folderId = "my-folder-id",
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )
            val library = listOf("my-song-id-1" to emptyList(), "my-song-id-2" to listOf("my-folder-id")).map {
                LibrarySong(
                    id = it.first,
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.IN_PROGRESS,
                    folderIds = it.second,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }
            val repository =
                TidyUpRepositoryImpl(
                    database,
                    FakeSongRepository(library = library),
                    FakeUserPreferencesRepository(),
                    clock,
                )

            // when
            repository.mergeDuplicates(keepId = "my-song-id-1", duplicates = listOf("my-song-id-2"))

            // then
            database.versionDao().getById("my-version-id")?.songId shouldBe "my-song-id-1"
            database.songFolderDao().getById("my-song-id-1:my-folder-id")?.deleted shouldBe false
            database.songDao().getById("my-song-id-2")?.deleted shouldBe true
        }
    }
}
