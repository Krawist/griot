package com.griot.feature.editor.audio

import com.griot.core.data.audio.ExternalMusicState
import com.griot.core.data.audio.FakeExternalMusic
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
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
internal class ExternalMusicViewModelTest {
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
        fun `should explain the access the first time another app's music shows without it`() = runTest {
            // given
            val viewModel = ExternalMusicViewModel(
                music = FakeExternalMusic(state = ExternalMusicState(active = true, playing = true)),
                preferences = FakeUserPreferencesRepository(),
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { it.music.active }

            // then
            state.explain shouldBe true
        }

        @Test
        fun `should not explain the access once it is given`() = runTest {
            // given
            val viewModel = ExternalMusicViewModel(
                music = FakeExternalMusic(state = ExternalMusicState(active = true, controlled = true)),
                preferences = FakeUserPreferencesRepository(),
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { it.music.active }

            // then
            state.explain shouldBe false
        }
    }

    @Nested
    inner class OnExplained {
        @Test
        fun `should not explain the access again`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            val viewModel = ExternalMusicViewModel(
                music = FakeExternalMusic(state = ExternalMusicState(active = true)),
                preferences = preferences,
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            viewModel.onExplained()

            // then
            viewModel.uiState.first { it.music.active }.explain shouldBe false
            preferences.externalMusicExplained.value shouldBe true
        }
    }

    @Nested
    inner class Controls {
        @Test
        fun `should play or pause, and go back 15 seconds`() = runTest {
            // given
            val music = FakeExternalMusic()
            val viewModel = ExternalMusicViewModel(music = music, preferences = FakeUserPreferencesRepository())

            // when
            viewModel.onPlayToggle()
            viewModel.onRewind()

            // then
            music.calls shouldContainExactly listOf("play pause", "rewind 15000")
        }
    }
}
