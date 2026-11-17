package com.griot.feature.library.folder

import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind
import com.griot.core.data.model.Genre
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.data.organisation.FakeFolderRepository
import com.griot.core.data.organisation.FakeGenreRepository
import com.griot.core.data.song.FakeSongRepository
import com.griot.core.phonetics.PhoneticEngines
import com.griot.feature.library.LibraryItemsImpl
import com.griot.feature.library.RECENT_FOLDER_ID
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class FolderViewModelTest {
    private val engines = PhoneticEngines()
    private val clock = Clock.fixed(Instant.parse("2026-11-16T16:00:00Z"), ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should list the songs of the folder only`() = runTest {
            // given
            val folder = Folder(
                id = "my-folder-id",
                name = "my-name",
                color = FolderColor.MAGENTA,
                kind = FolderKind.CUSTOM,
                songCount = 2,
            )
            val library = listOf(
                LibrarySong(
                    id = "my-song-id-1",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("my-folder-id"),
                    genreId = "afro",
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
                LibrarySong(
                    id = "my-song-id-2",
                    title = "my-title",
                    language = SongLanguage.ENGLISH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("my-folder-id"),
                    genreId = "rap",
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
                LibrarySong(
                    id = "my-song-id-3",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = emptyList(),
                    genreId = null,
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
            )
            val viewModel = FolderViewModel(
                folderId = "my-folder-id",
                songs = FakeSongRepository(library = library),
                folders = FakeFolderRepository(folders = listOf(folder)),
                genres = FakeGenreRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.items.map { it.song.id } shouldContainExactly listOf("my-song-id-1", "my-song-id-2")
            state.songCount shouldBe 2
        }

        @Test
        fun `should list every song for all the texts`() = runTest {
            // given
            val folder = Folder(
                id = "my-folder-id",
                name = "my-name",
                color = FolderColor.MAGENTA,
                kind = FolderKind.CUSTOM,
                songCount = 2,
            )
            val library = listOf(
                LibrarySong(
                    id = "my-song-id-1",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("my-folder-id"),
                    genreId = "afro",
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
                LibrarySong(
                    id = "my-song-id-2",
                    title = "my-title",
                    language = SongLanguage.ENGLISH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("my-folder-id"),
                    genreId = "rap",
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
                LibrarySong(
                    id = "my-song-id-3",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = emptyList(),
                    genreId = null,
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
            )
            val viewModel = FolderViewModel(
                folderId = null,
                songs = FakeSongRepository(library = library),
                folders = FakeFolderRepository(folders = listOf(folder)),
                genres = FakeGenreRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.items.map { it.song.id } shouldContainExactly listOf("my-song-id-1", "my-song-id-2", "my-song-id-3")
        }

        @Test
        fun `should list the songs changed in the last 24 hours for Récents`() = runTest {
            // given
            val library = listOf("2026-11-16T14:00:00Z", "2026-11-15T15:00:00Z").mapIndexed { index, updated ->
                LibrarySong(
                    id = "my-song-id-${index + 1}",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    updatedAt = Instant.parse(updated),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }
            val viewModel = FolderViewModel(
                folderId = RECENT_FOLDER_ID,
                songs = FakeSongRepository(library = library),
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.items.map { it.song.id } shouldContainExactly listOf("my-song-id-1")
            state.recent shouldBe true
            state.missing shouldBe false
        }

        @Test
        fun `should offer the genres of its songs only`() = runTest {
            // given
            val folder = Folder(
                id = "my-folder-id",
                name = "my-name",
                color = FolderColor.MAGENTA,
                kind = FolderKind.CUSTOM,
                songCount = 2,
            )
            val library = listOf(
                LibrarySong(
                    id = "my-song-id-1",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("my-folder-id"),
                    genreId = "afro",
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
                LibrarySong(
                    id = "my-song-id-2",
                    title = "my-title",
                    language = SongLanguage.ENGLISH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("my-folder-id"),
                    genreId = "rap",
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
            )
            val viewModel = FolderViewModel(
                folderId = "my-folder-id",
                songs = FakeSongRepository(library = library),
                folders = FakeFolderRepository(folders = listOf(folder)),
                genres = FakeGenreRepository(
                    genres = listOf("afro", "rap", "pop").map {
                        Genre(id = it, name = it, builtIn = null)
                    },
                ),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.genres.map { it.id } shouldContainExactly listOf("afro", "rap")
        }

        @Test
        fun `should close when the folder no longer exists`() = runTest {
            // given
            val viewModel = FolderViewModel(
                folderId = "my-folder-id",
                songs = FakeSongRepository(),
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.missing shouldBe true
        }
    }

    @Nested
    inner class OnGenreSelected {
        @Test
        fun `should keep the songs of the genre`() = runTest {
            // given
            val folder = Folder(
                id = "my-folder-id",
                name = "my-name",
                color = FolderColor.MAGENTA,
                kind = FolderKind.CUSTOM,
                songCount = 2,
            )
            val library = listOf(
                LibrarySong(
                    id = "my-song-id-1",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("my-folder-id"),
                    genreId = "afro",
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
                LibrarySong(
                    id = "my-song-id-2",
                    title = "my-title",
                    language = SongLanguage.ENGLISH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("my-folder-id"),
                    genreId = "rap",
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
                LibrarySong(
                    id = "my-song-id-3",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = emptyList(),
                    genreId = null,
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
            )
            val viewModel = FolderViewModel(
                folderId = "my-folder-id",
                songs = FakeSongRepository(library = library),
                folders = FakeFolderRepository(folders = listOf(folder)),
                genres = FakeGenreRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.onGenreSelected("rap")

            // then
            viewModel.uiState.first { it.genre == "rap" }.items.map { it.song.id } shouldContainExactly
                listOf("my-song-id-2")
        }
    }

    @Nested
    inner class OnLanguageSelected {
        @Test
        fun `should keep the songs in the language`() = runTest {
            // given
            val folder = Folder(
                id = "my-folder-id",
                name = "my-name",
                color = FolderColor.MAGENTA,
                kind = FolderKind.CUSTOM,
                songCount = 2,
            )
            val library = listOf(
                LibrarySong(
                    id = "my-song-id-1",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("my-folder-id"),
                    genreId = "afro",
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
                LibrarySong(
                    id = "my-song-id-2",
                    title = "my-title",
                    language = SongLanguage.ENGLISH,
                    status = SongStatus.DRAFT,
                    folderIds = listOf("my-folder-id"),
                    genreId = "rap",
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
                LibrarySong(
                    id = "my-song-id-3",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    folderIds = emptyList(),
                    genreId = null,
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                ),
            )
            val viewModel = FolderViewModel(
                folderId = "my-folder-id",
                songs = FakeSongRepository(library = library),
                folders = FakeFolderRepository(folders = listOf(folder)),
                genres = FakeGenreRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.onLanguageSelected(SongLanguage.FRENCH)

            // then
            viewModel.uiState.first { it.language != null }.items.map { it.song.id } shouldContainExactly
                listOf("my-song-id-1")
        }
    }

    @Nested
    inner class OnToggleSelected {
        @Test
        fun `should select the songs tapped in the multiple selection`() = runTest {
            // given
            val folder = Folder(
                id = "my-folder-id",
                name = "my-name",
                color = FolderColor.MAGENTA,
                kind = FolderKind.CUSTOM,
                songCount = 2,
            )
            val viewModel = FolderViewModel(
                folderId = "my-folder-id",
                songs = FakeSongRepository(),
                folders = FakeFolderRepository(folders = listOf(folder)),
                genres = FakeGenreRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.onSelecting(true)

            // when
            viewModel.onToggleSelected("my-song-id-1")
            viewModel.onToggleSelected("my-song-id-2")
            viewModel.onToggleSelected("my-song-id-1")

            // then
            viewModel.uiState.first { it.selecting }.selected shouldBe setOf("my-song-id-2")
        }

        @Test
        fun `should leave the selection when it is turned off`() = runTest {
            // given
            val folder = Folder(
                id = "my-folder-id",
                name = "my-name",
                color = FolderColor.MAGENTA,
                kind = FolderKind.CUSTOM,
                songCount = 2,
            )
            val viewModel = FolderViewModel(
                folderId = "my-folder-id",
                songs = FakeSongRepository(),
                folders = FakeFolderRepository(folders = listOf(folder)),
                genres = FakeGenreRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.onSelecting(true)
            viewModel.onToggleSelected("my-song-id-1")

            // when
            viewModel.onSelecting(false)

            // then
            viewModel.uiState.first { !it.loading }.selected.shouldBeEmpty()
        }
    }

    @Nested
    inner class FolderChanges {
        @Test
        fun `should rename, recolour and delete the folder`() = runTest {
            // given
            val folder = Folder(
                id = "my-folder-id",
                name = "my-name",
                color = FolderColor.MAGENTA,
                kind = FolderKind.CUSTOM,
                songCount = 2,
            )
            val folders = FakeFolderRepository(folders = listOf(folder))
            val viewModel = FolderViewModel(
                folderId = "my-folder-id",
                songs = FakeSongRepository(),
                folders = folders,
                genres = FakeGenreRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.onRenameFolder("my-name-2")
            viewModel.onFolderColor(FolderColor.ORANGE)
            viewModel.onDeleteFolder()

            // then
            folders.calls shouldContainExactly listOf(
                "rename my-folder-id my-name-2",
                "color my-folder-id ORANGE",
                "delete my-folder-id",
            )
        }

        @Test
        fun `should change nothing for all the texts`() = runTest {
            // given
            val folders = FakeFolderRepository()
            val viewModel = FolderViewModel(
                folderId = null,
                songs = FakeSongRepository(),
                folders = folders,
                genres = FakeGenreRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.onRenameFolder("my-name")
            viewModel.onDeleteFolder()

            // then
            folders.calls.shouldBeEmpty()
        }
    }
}
