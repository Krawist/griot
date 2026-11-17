package com.griot.feature.library

import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.data.organisation.FakeFolderRepository
import com.griot.core.data.organisation.FakeGenreRepository
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import com.griot.core.data.song.FakeSongRepository
import com.griot.core.design.format.RelativeTime
import com.griot.core.phonetics.PhoneticEngines
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
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
internal class LibraryViewModelTest {
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
        fun `should show the songs changed in the last 24 hours`() = runTest {
            // given
            val today = LibrarySong(
                id = "my-song-id-1",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                sectionCount = 1,
                firstVerse = null,
                lyrics = "my-lyrics",
                firstSection = emptyList(),
            )
            val older = LibrarySong(
                id = "my-song-id-2",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                updatedAt = Instant.parse("2026-11-15T15:00:00Z"),
                sectionCount = 1,
                firstVerse = null,
                lyrics = "my-lyrics",
                firstSection = emptyList(),
            )
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(library = listOf(today, older)),
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.today shouldContainExactly listOf(LibraryItem(song = today, updated = RelativeTime.HoursAgo(2)))
            state.resume.shouldBeNull()
            state.songCount shouldBe 2
        }

        @Test
        fun `should offer to pick up the last song touched when nothing changed today`() = runTest {
            // given
            val song = LibrarySong(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                updatedAt = Instant.parse("2026-11-13T15:00:00Z"),
                sectionCount = 1,
                firstVerse = null,
                lyrics = "my-lyrics",
                firstSection = listOf("my-verse-1", "", "my-verse-2", "my-verse-3"),
            )
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(library = listOf(song)),
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.today.shouldBeEmpty()
            state.resume?.let { it.item.song to it.verses } shouldBe (song to listOf("my-verse-1", "my-verse-2"))
        }

        @Test
        fun `should list the folders`() = runTest {
            // given
            val folder = Folder(
                id = "my-folder-id",
                name = "my-name",
                color = FolderColor.MAGENTA,
                kind = FolderKind.CUSTOM,
                songCount = 0,
            )
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(),
                folders = FakeFolderRepository(folders = listOf(folder)),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.folders shouldContainExactly listOf(folder)
        }

        @Test
        fun `should not greet a blank name`() = runTest {
            // given
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(),
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(authorName = " "),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.authorName.shouldBeNull()
        }
    }

    @Nested
    inner class OnQueryChange {
        @Test
        fun `should keep only the songs matching the search`() = runTest {
            // given
            val song = LibrarySong(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                sectionCount = 1,
                firstVerse = null,
                lyrics = "my-lyrics",
                firstSection = emptyList(),
            )
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(library = listOf(song, song.copy(id = "my-song-id-2", title = "other"))),
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.onQueryChange("my-title")

            // then
            viewModel.uiState.first { it.query == "my-title" }.results.map { it.song.id } shouldContainExactly
                listOf("my-song-id")
        }
    }

    @Nested
    inner class OnNewFolder {
        @Test
        fun `should create the folder in the next accent colour`() = runTest {
            // given
            val folders = FakeFolderRepository(
                folders = listOf(
                    Folder(
                        id = "my-folder-id",
                        name = "my-name-1",
                        color = FolderColor.MAGENTA,
                        kind = FolderKind.CUSTOM,
                        songCount = 0,
                    ),
                ),
            )
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(),
                folders = folders,
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onNewFolder("my-name-2")

            // then
            folders.calls shouldContainExactly listOf("create my-name-2 TURQUOISE")
        }

        @Test
        fun `should not create a folder without a name`() = runTest {
            // given
            val folders = FakeFolderRepository()
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(),
                folders = folders,
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.onNewFolder(" ")

            // then
            folders.calls.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnNewSong {
        @Test
        fun `should open the song created`() = runTest {
            // given
            val songs = FakeSongRepository()
            val viewModel = LibraryViewModel(
                songs = songs,
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            var opened: String? = null

            // when
            viewModel.onNewSong(onCreated = { opened = it })

            // then
            opened shouldBe "created-song-id-1"
            songs.calls shouldContainExactly listOf("create created-song-id-1")
        }
    }

    @Nested
    inner class OnRename {
        @Test
        fun `should rename the song without the spaces around the title`() = runTest {
            // given
            val songs = FakeSongRepository()
            val viewModel = LibraryViewModel(
                songs = songs,
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )

            // when
            viewModel.actions.onRename(id = "my-song-id", title = " my-title ")

            // then
            songs.calls shouldContainExactly listOf("rename my-song-id my-title")
        }
    }

    @Nested
    inner class OnDuplicate {
        @Test
        fun `should copy the song under the title given`() = runTest {
            // given
            val songs = FakeSongRepository()
            val viewModel = LibraryViewModel(
                songs = songs,
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )

            // when
            viewModel.actions.onDuplicate(id = "my-song-id", title = "my-title")

            // then
            songs.calls shouldContainExactly listOf("duplicate my-song-id my-title")
        }
    }

    @Nested
    inner class OnDelete {
        @Test
        fun `should move the song to the trash and offer to undo`() = runTest {
            // given
            val songs = FakeSongRepository()
            val viewModel = LibraryViewModel(
                songs = songs,
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.actions.onDelete(id = "my-song-id", title = "my-title")

            // then
            viewModel.uiState.first { !it.loading }.deleted shouldBe DeletedSong(id = "my-song-id", title = "my-title")
            songs.calls shouldContainExactly listOf("delete my-song-id")
        }
    }

    @Nested
    inner class OnUndoDelete {
        @Test
        fun `should take the song just deleted out of the trash`() = runTest {
            // given
            val songs = FakeSongRepository()
            val viewModel = LibraryViewModel(
                songs = songs,
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.actions.onDelete(id = "my-song-id", title = "my-title")

            // when
            viewModel.actions.onUndoDelete()

            // then
            viewModel.uiState.first { !it.loading }.deleted.shouldBeNull()
            songs.calls shouldContainExactly listOf("delete my-song-id", "restore my-song-id")
        }
    }

    @Nested
    inner class OnUndoDismissed {
        @Test
        fun `should hide the undo bar and keep the song in the trash`() = runTest {
            // given
            val songs = FakeSongRepository()
            val viewModel = LibraryViewModel(
                songs = songs,
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                preferences = FakeUserPreferencesRepository(),
                items = LibraryItemsImpl(engines = engines, clock = clock),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.actions.onDelete(id = "my-song-id", title = "my-title")

            // when
            viewModel.actions.onUndoDismissed()

            // then
            viewModel.uiState.first { !it.loading }.deleted.shouldBeNull()
            songs.calls shouldContainExactly listOf("delete my-song-id")
        }
    }
}
