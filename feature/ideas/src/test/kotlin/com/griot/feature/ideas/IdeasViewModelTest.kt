package com.griot.feature.ideas

import com.griot.core.data.idea.FakeIdeaRepository
import com.griot.core.data.model.Idea
import com.griot.core.data.song.FakeSongRepository
import com.griot.core.design.format.RelativeTime
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
internal class IdeasViewModelTest {
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
        fun `should list the ideas with when they were saved`() = runTest {
            // given
            val idea = Idea(
                id = "my-idea-id",
                text = "my-text",
                imagePath = null,
                songId = null,
                songTitle = null,
                createdAt = Instant.parse("2026-11-16T15:30:00Z"),
            )
            val viewModel =
                IdeasViewModel(ideas = FakeIdeaRepository(listOf(idea)), songs = FakeSongRepository(), clock = clock)
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.items shouldContainExactly listOf(IdeaItem(idea = idea, saved = RelativeTime.MinutesAgo(30)))
        }
    }

    @Nested
    inner class OnAdd {
        @Test
        fun `should save the idea`() = runTest {
            // given
            val ideas = FakeIdeaRepository()
            val viewModel = IdeasViewModel(ideas = ideas, songs = FakeSongRepository(), clock = clock)

            // when
            viewModel.onAdd("my-text")

            // then
            ideas.calls shouldContainExactly listOf("add my-text")
        }

        @Test
        fun `should not save a blank idea`() = runTest {
            // given
            val ideas = FakeIdeaRepository()
            val viewModel = IdeasViewModel(ideas = ideas, songs = FakeSongRepository(), clock = clock)

            // when
            viewModel.onAdd("  ")

            // then
            ideas.calls.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnStartSong {
        @Test
        fun `should start a song from the idea and open it`() = runTest {
            // given
            val ideas = FakeIdeaRepository()
            val viewModel = IdeasViewModel(ideas = ideas, songs = FakeSongRepository(), clock = clock)
            var opened: String? = null

            // when
            viewModel.onStartSong(id = "my-idea-id", onCreated = { opened = it })

            // then
            opened shouldBe "started-song-id"
            ideas.calls shouldContainExactly listOf("start my-idea-id")
        }
    }

    @Nested
    inner class OnDelete {
        @Test
        fun `should delete the idea, then bring it back on undo`() = runTest {
            // given
            val idea = Idea(
                id = "my-idea-id",
                text = "my-text",
                imagePath = null,
                songId = null,
                songTitle = null,
                createdAt = Instant.parse("2026-11-16T15:30:00Z"),
            )
            val ideas = FakeIdeaRepository()
            val viewModel = IdeasViewModel(ideas = ideas, songs = FakeSongRepository(), clock = clock)
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.onDelete(idea)

            // when
            viewModel.onUndoDelete()

            // then
            viewModel.uiState.first { !it.loading }.deleted.shouldBeNull()
            ideas.calls shouldContainExactly listOf("delete my-idea-id", "restore my-idea-id")
        }
    }
}
