package com.griot.feature.library

import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import com.griot.core.data.song.FakeSongRepository
import com.griot.core.design.format.RelativeTime
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
        fun `should list the songs with when they last changed`() = runTest {
            // given
            val song = LibrarySong(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                sectionCount = 1,
                firstVerse = "my-first-verse",
                lyrics = "my-lyrics",
            )
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(library = listOf(song)),
                preferences = FakeUserPreferencesRepository(authorName = "my-author-name"),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state shouldBe LibraryUiState(
                authorName = "my-author-name",
                items = listOf(LibraryItem(song = song, updated = RelativeTime.HoursAgo(2))),
                hasSongs = true,
                loading = false,
            )
        }

        @Test
        fun `should not greet a blank name`() = runTest {
            // given
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(),
                preferences = FakeUserPreferencesRepository(authorName = " "),
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
            val neons = LibrarySong(
                id = "my-song-id-1",
                title = "Les néons",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                sectionCount = 1,
                firstVerse = null,
                lyrics = "",
            )
            val dry = neons.copy(id = "my-song-id-2", title = "Saison sèche")
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(library = listOf(neons, dry)),
                preferences = FakeUserPreferencesRepository(),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.onQueryChange("neons")

            // then
            val state = viewModel.uiState.first { !it.loading }
            state.query shouldBe "neons"
            state.items.map { it.song } shouldContainExactly listOf(neons)
            state.hasSongs shouldBe true
        }
    }

    @Nested
    inner class OnFilterSelected {
        @Test
        fun `should keep only the songs under the filter`() = runTest {
            // given
            val draft = LibrarySong(
                id = "my-song-id-1",
                title = "my-title-1",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                sectionCount = 1,
                firstVerse = null,
                lyrics = "",
            )
            val inProgress = draft.copy(id = "my-song-id-2", title = "my-title-2", status = SongStatus.IN_PROGRESS)
            val viewModel = LibraryViewModel(
                songs = FakeSongRepository(library = listOf(draft, inProgress)),
                preferences = FakeUserPreferencesRepository(),
                clock = clock,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.onFilterSelected(LibraryFilter.DRAFTS)

            // then
            val state = viewModel.uiState.first { !it.loading }
            state.filter shouldBe LibraryFilter.DRAFTS
            state.items.map { it.song } shouldContainExactly listOf(draft)
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
                preferences = FakeUserPreferencesRepository(),
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
            val viewModel =
                LibraryViewModel(songs = songs, preferences = FakeUserPreferencesRepository(), clock = clock)

            // when
            viewModel.onRename(id = "my-song-id", title = " my-title ")

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
            val viewModel =
                LibraryViewModel(songs = songs, preferences = FakeUserPreferencesRepository(), clock = clock)

            // when
            viewModel.onDuplicate(id = "my-song-id", title = "my-title")

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
            val viewModel =
                LibraryViewModel(songs = songs, preferences = FakeUserPreferencesRepository(), clock = clock)
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.onDelete(id = "my-song-id", title = "my-title")

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
            val viewModel =
                LibraryViewModel(songs = songs, preferences = FakeUserPreferencesRepository(), clock = clock)
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.onDelete(id = "my-song-id", title = "my-title")

            // when
            viewModel.onUndoDelete()

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
            val viewModel =
                LibraryViewModel(songs = songs, preferences = FakeUserPreferencesRepository(), clock = clock)
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.onDelete(id = "my-song-id", title = "my-title")

            // when
            viewModel.onUndoDismissed()

            // then
            viewModel.uiState.first { !it.loading }.deleted.shouldBeNull()
            songs.calls shouldContainExactly listOf("delete my-song-id")
        }
    }
}
