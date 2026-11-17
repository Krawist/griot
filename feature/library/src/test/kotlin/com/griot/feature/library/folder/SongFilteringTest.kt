package com.griot.feature.library.folder

import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SongFilteringTest {
    @Nested
    inner class FilteredBy {
        @Test
        fun `should keep the songs having one of the genres`() {
            // given
            val songs = listOf(listOf("rap"), listOf("afro", "pop"), emptyList()).mapIndexed { index, genres ->
                LibrarySong(
                    id = "my-song-id-${index + 1}",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    genreIds = genres,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }

            // when
            val kept = songs.filteredBy(FolderFilters(genres = setOf("rap", "pop")))

            // then
            kept.map { it.id } shouldContainExactly listOf("my-song-id-1", "my-song-id-2")
        }

        @Test
        fun `should leave out the archived songs until they are included`() {
            // given
            val songs = listOf(listOf("archives"), emptyList()).mapIndexed { index, folders ->
                LibrarySong(
                    id = "my-song-id-${index + 1}",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = folders,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }

            // when
            val left = songs.filteredBy(FolderFilters())
            val included = songs.filteredBy(FolderFilters(includeArchives = true))

            // then
            left.map { it.id } shouldContainExactly listOf("my-song-id-2")
            included.map { it.id } shouldContainExactly listOf("my-song-id-1", "my-song-id-2")
        }

        @Test
        fun `should keep the archived songs in the Archives folder`() {
            // given
            val songs = listOf(
                LibrarySong(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("archives"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
            )

            // when
            val kept = songs.filteredBy(FolderFilters(), archives = true)

            // then
            kept.map { it.id } shouldContainExactly listOf("my-song-id")
        }

        @Test
        fun `should keep the songs in no folder`() {
            // given
            val songs = listOf(listOf("my-folder-id"), emptyList()).mapIndexed { index, folders ->
                LibrarySong(
                    id = "my-song-id-${index + 1}",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = folders,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }

            // when
            val kept = songs.filteredBy(FolderFilters(folder = FolderFilters.NO_FOLDER))

            // then
            kept.map { it.id } shouldContainExactly listOf("my-song-id-2")
        }

        @Test
        fun `should keep the songs in one of the states`() {
            // given
            val songs = listOf(
                "my-title" to SongStatus.DRAFT,
                "my-title" to SongStatus.IN_PROGRESS,
                "" to SongStatus.IN_PROGRESS,
            ).mapIndexed { index, (title, status) ->
                LibrarySong(
                    id = "my-song-id-${index + 1}",
                    title = title,
                    language = SongLanguage.FRENCH,
                    status = status,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }

            // when
            val kept = songs.filteredBy(FolderFilters(states = setOf(SongState.DRAFT, SongState.UNTITLED)))

            // then
            kept.map { it.id } shouldContainExactly listOf("my-song-id-1", "my-song-id-3")
        }

        @Test
        fun `should order by title, accents aside, the songs without one last`() {
            // given
            val songs = listOf("Zénith", "", "étoile", "Avion").mapIndexed { index, title ->
                LibrarySong(
                    id = "my-song-id-${index + 1}",
                    title = title,
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }

            // when
            val kept = songs.filteredBy(FolderFilters(sort = SongSort.TITLE))

            // then
            kept.map { it.title } shouldContainExactly listOf("Avion", "étoile", "Zénith", "")
        }

        @Test
        fun `should order by creation, the last created first`() {
            // given
            val songs = listOf("2026-11-01T10:00:00Z", "2026-11-10T10:00:00Z").mapIndexed { index, created ->
                LibrarySong(
                    id = "my-song-id-${index + 1}",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    createdAt = Instant.parse(created),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }

            // when
            val kept = songs.filteredBy(FolderFilters(sort = SongSort.CREATED))

            // then
            kept.map { it.id } shouldContainExactly listOf("my-song-id-2", "my-song-id-1")
        }
    }

    @Nested
    inner class ActiveCount {
        @Test
        fun `should count the filters on, the order included`() {
            // when
            val count = FolderFilters(
                sort = SongSort.TITLE,
                genres = setOf("rap", "pop"),
                language = SongLanguage.ENGLISH,
                includeArchives = true,
            ).activeCount()

            // then
            count shouldBe 4
        }

        @Test
        fun `should count none without a filter`() {
            // when
            val count = FolderFilters().activeCount()

            // then
            count shouldBe 0
        }
    }
}
