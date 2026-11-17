package com.griot.feature.library.filing

import com.griot.core.data.model.BuiltInGenre
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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Instant
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
internal class FilingViewModelTest {
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
        fun `should check the folders holding every song, and mark those holding some of them`() = runTest {
            // given
            val songs = FakeSongRepository(
                library = listOf(
                    LibrarySong(
                        id = "my-song-id-1",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1", "my-folder-id-2"),
                        genreIds = listOf("afro"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                    LibrarySong(
                        id = "my-song-id-2",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1"),
                        genreIds = listOf("rap"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                ),
            )
            val folders = FakeFolderRepository(
                folders = listOf("my-folder-id-1", "my-folder-id-2", "my-folder-id-3").map {
                    Folder(id = it, name = it, color = FolderColor.MAGENTA, kind = FolderKind.CUSTOM, songCount = 1)
                },
            )
            val viewModel = FilingViewModel(
                songIds = listOf("my-song-id-1", "my-song-id-2"),
                songs = songs,
                folders = folders,
                genres = FakeGenreRepository(
                    genres = listOf(Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO)),
                ),
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            val state = viewModel.uiState.value

            // then
            state.folders.map { it.check } shouldContainExactly
                listOf(FilingCheck.CHECKED, FilingCheck.MIXED, FilingCheck.UNCHECKED)
        }

        @Test
        fun `should show a genre some of the songs have as mixed`() = runTest {
            // given
            val songs = FakeSongRepository(
                library = listOf(
                    LibrarySong(
                        id = "my-song-id-1",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1", "my-folder-id-2"),
                        genreIds = listOf("afro"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                    LibrarySong(
                        id = "my-song-id-2",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1"),
                        genreIds = listOf("rap"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                ),
            )
            val folders = FakeFolderRepository(
                folders = listOf("my-folder-id-1", "my-folder-id-2", "my-folder-id-3").map {
                    Folder(id = it, name = it, color = FolderColor.MAGENTA, kind = FolderKind.CUSTOM, songCount = 1)
                },
            )
            val viewModel = FilingViewModel(
                songIds = listOf("my-song-id-1", "my-song-id-2"),
                songs = songs,
                folders = folders,
                genres = FakeGenreRepository(
                    genres = listOf(Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO)),
                ),
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            val state = viewModel.uiState.value

            // then
            state.genres.map { it.check } shouldContainExactly listOf(FilingCheck.MIXED)
            state.songCount shouldBe 2
        }
    }

    @Nested
    inner class OnFolderTapped {
        @Test
        fun `should check a folder holding some of the songs, and uncheck one holding all of them`() = runTest {
            // given
            val songs = FakeSongRepository(
                library = listOf(
                    LibrarySong(
                        id = "my-song-id-1",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1", "my-folder-id-2"),
                        genreIds = listOf("afro"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                    LibrarySong(
                        id = "my-song-id-2",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1"),
                        genreIds = listOf("rap"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                ),
            )
            val folders = FakeFolderRepository(
                folders = listOf("my-folder-id-1", "my-folder-id-2", "my-folder-id-3").map {
                    Folder(id = it, name = it, color = FolderColor.MAGENTA, kind = FolderKind.CUSTOM, songCount = 1)
                },
            )
            val viewModel = FilingViewModel(
                songIds = listOf("my-song-id-1", "my-song-id-2"),
                songs = songs,
                folders = folders,
                genres = FakeGenreRepository(
                    genres = listOf(Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO)),
                ),
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onFolderTapped("my-folder-id-2")
            viewModel.onFolderTapped("my-folder-id-1")

            // then
            viewModel.uiState.value.folders.map { it.check } shouldContainExactly
                listOf(FilingCheck.UNCHECKED, FilingCheck.CHECKED, FilingCheck.UNCHECKED)
        }
    }

    @Nested
    inner class OnNoFolder {
        @Test
        fun `should uncheck every folder`() = runTest {
            // given
            val songs = FakeSongRepository(
                library = listOf(
                    LibrarySong(
                        id = "my-song-id-1",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1", "my-folder-id-2"),
                        genreIds = listOf("afro"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                    LibrarySong(
                        id = "my-song-id-2",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1"),
                        genreIds = listOf("rap"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                ),
            )
            val folders = FakeFolderRepository(
                folders = listOf("my-folder-id-1", "my-folder-id-2", "my-folder-id-3").map {
                    Folder(id = it, name = it, color = FolderColor.MAGENTA, kind = FolderKind.CUSTOM, songCount = 1)
                },
            )
            val viewModel = FilingViewModel(
                songIds = listOf("my-song-id-1", "my-song-id-2"),
                songs = songs,
                folders = folders,
                genres = FakeGenreRepository(
                    genres = listOf(Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO)),
                ),
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onNoFolder()

            // then
            viewModel.uiState.value.folders.map { it.check }.distinct() shouldContainExactly
                listOf(FilingCheck.UNCHECKED)
        }
    }

    @Nested
    inner class OnGenreTapped {
        @Test
        fun `should check the genre, and uncheck it when tapped again`() = runTest {
            // given
            val songs = FakeSongRepository(
                library = listOf(
                    LibrarySong(
                        id = "my-song-id-1",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1", "my-folder-id-2"),
                        genreIds = listOf("afro"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                    LibrarySong(
                        id = "my-song-id-2",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1"),
                        genreIds = listOf("rap"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                ),
            )
            val folders = FakeFolderRepository(
                folders = listOf("my-folder-id-1", "my-folder-id-2", "my-folder-id-3").map {
                    Folder(id = it, name = it, color = FolderColor.MAGENTA, kind = FolderKind.CUSTOM, songCount = 1)
                },
            )
            val viewModel = FilingViewModel(
                songIds = listOf("my-song-id-1", "my-song-id-2"),
                songs = songs,
                folders = folders,
                genres = FakeGenreRepository(
                    genres = listOf(Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO)),
                ),
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onGenreTapped("afro")
            val checked = viewModel.uiState.value.genres.single().check
            viewModel.onGenreTapped("afro")

            // then
            checked shouldBe FilingCheck.CHECKED
            viewModel.uiState.value.genres.single().check shouldBe FilingCheck.UNCHECKED
        }
    }

    @Nested
    inner class OnValidate {
        @Test
        fun `should apply the folders and the genres tapped`() = runTest {
            // given
            val songs = FakeSongRepository(
                library = listOf(
                    LibrarySong(
                        id = "my-song-id-1",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1", "my-folder-id-2"),
                        genreIds = listOf("afro"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                    LibrarySong(
                        id = "my-song-id-2",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1"),
                        genreIds = listOf("rap"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                ),
            )
            val folders = FakeFolderRepository(
                folders = listOf("my-folder-id-1", "my-folder-id-2", "my-folder-id-3").map {
                    Folder(id = it, name = it, color = FolderColor.MAGENTA, kind = FolderKind.CUSTOM, songCount = 1)
                },
            )
            val genres =
                FakeGenreRepository(genres = listOf(Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO)))
            val viewModel = FilingViewModel(
                songIds = listOf("my-song-id-1", "my-song-id-2"),
                songs = songs,
                folders = folders,
                genres = genres,
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }
            viewModel.onFolderTapped("my-folder-id-3")
            viewModel.onFolderTapped("my-folder-id-1")
            viewModel.onGenreTapped("afro")
            var done = false

            // when
            viewModel.onValidate(onDone = { done = true })

            // then
            done shouldBe true
            folders.calls shouldContainExactly listOf(
                "file my-song-id-1, my-song-id-2 +my-folder-id-3 -my-folder-id-1",
            )
            genres.calls shouldContainExactly listOf("tag my-song-id-1, my-song-id-2 +afro -")
        }

        @Test
        fun `should change nothing that was not tapped`() = runTest {
            // given
            val songs = FakeSongRepository(
                library = listOf(
                    LibrarySong(
                        id = "my-song-id-1",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1", "my-folder-id-2"),
                        genreIds = listOf("afro"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                    LibrarySong(
                        id = "my-song-id-2",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1"),
                        genreIds = listOf("rap"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                ),
            )
            val folders = FakeFolderRepository(
                folders = listOf("my-folder-id-1", "my-folder-id-2", "my-folder-id-3").map {
                    Folder(id = it, name = it, color = FolderColor.MAGENTA, kind = FolderKind.CUSTOM, songCount = 1)
                },
            )
            val genres =
                FakeGenreRepository(genres = listOf(Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO)))
            val viewModel = FilingViewModel(
                songIds = listOf("my-song-id-1", "my-song-id-2"),
                songs = songs,
                folders = folders,
                genres = genres,
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onValidate(onDone = {})

            // then
            folders.calls.shouldBeEmpty()
            genres.calls.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnNewFolder {
        @Test
        fun `should create the folder and check it`() = runTest {
            // given
            val songs = FakeSongRepository(
                library = listOf(
                    LibrarySong(
                        id = "my-song-id-1",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1", "my-folder-id-2"),
                        genreIds = listOf("afro"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                    LibrarySong(
                        id = "my-song-id-2",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        folderIds = listOf("my-folder-id-1"),
                        genreIds = listOf("rap"),
                        updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    ),
                ),
            )
            val folders = FakeFolderRepository(
                folders = listOf("my-folder-id-1", "my-folder-id-2", "my-folder-id-3").map {
                    Folder(id = it, name = it, color = FolderColor.MAGENTA, kind = FolderKind.CUSTOM, songCount = 1)
                },
            )
            val viewModel = FilingViewModel(
                songIds = listOf("my-song-id-1", "my-song-id-2"),
                songs = songs,
                folders = folders,
                genres = FakeGenreRepository(
                    genres = listOf(Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO)),
                ),
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onNewFolder("my-name")
            folders.folders.value += Folder(
                id = "created-folder-id",
                name = "my-name",
                color = FolderColor.TURQUOISE,
                kind = FolderKind.CUSTOM,
                songCount = 0,
            )

            // then
            folders.calls shouldContainExactly listOf("create my-name")
            viewModel.uiState.value.folders.last().check shouldBe FilingCheck.CHECKED
        }
    }
}
