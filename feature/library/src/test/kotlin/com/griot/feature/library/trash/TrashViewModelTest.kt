package com.griot.feature.library.trash

import com.griot.core.data.model.TrashedSong
import com.griot.core.data.song.FakeSongRepository
import com.griot.core.design.format.RelativeTime
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrashViewModelTest {
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
        fun `should list the songs in the trash with when they were deleted`() = runTest {
            // given
            val song =
                TrashedSong(id = "my-song-id", title = "my-title", deletedAt = Instant.parse("2026-11-15T16:00:00Z"))
            val viewModel = TrashViewModel(songs = FakeSongRepository(trash = listOf(song)), clock = clock)
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state shouldBe TrashUiState(
                items = listOf(TrashItem(song = song, deleted = RelativeTime.Yesterday, daysLeft = 29)),
                loading = false,
            )
        }

        @ParameterizedTest
        @CsvSource(
            "2026-11-16T15:00:00Z, 30",
            "2026-10-19T16:00:00Z, 2",
            "2026-10-17T20:00:00Z, 1",
            "2026-10-17T15:00:00Z, 1",
        )
        fun `should count the days left, rounded up and at least one`(deletedAt: String, daysLeft: Long) {
            runTest {
                // given
                val song = TrashedSong(id = "my-song-id", title = "my-title", deletedAt = Instant.parse(deletedAt))
                val viewModel = TrashViewModel(songs = FakeSongRepository(trash = listOf(song)), clock = clock)
                viewModel.uiState.launchIn(backgroundScope)

                // when
                val state = viewModel.uiState.first { !it.loading }

                // then
                state.items.single().daysLeft shouldBe daysLeft
            }
        }
    }

    @Nested
    inner class OnRestore {
        @Test
        fun `should take the song out of the trash`() = runTest {
            // given
            val songs = FakeSongRepository()
            val viewModel = TrashViewModel(songs = songs, clock = clock)

            // when
            viewModel.onRestore("my-song-id")

            // then
            songs.calls shouldContainExactly listOf("restore my-song-id")
        }
    }
}
