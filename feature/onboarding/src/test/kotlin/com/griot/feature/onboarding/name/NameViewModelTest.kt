package com.griot.feature.onboarding.name

import com.griot.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class NameViewModelTest {
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
        fun `should start with the name given before`() {
            // given
            val preferences = FakeUserPreferencesRepository(authorName = "my-name")

            // when
            val viewModel = NameViewModel(preferences)

            // then
            viewModel.uiState.value shouldBe NameUiState(name = "my-name")
        }
    }

    @Nested
    inner class OnNameChange {
        @Test
        fun `should not point out an empty name before a first attempt`() {
            // given
            val viewModel = NameViewModel(FakeUserPreferencesRepository())

            // when
            viewModel.onNameChange("")

            // then
            viewModel.uiState.value.emptyNameShown.shouldBeFalse()
        }

        @Test
        fun `should stop pointing out the name once one is typed`() {
            // given
            val viewModel = NameViewModel(FakeUserPreferencesRepository())
            viewModel.onContinue(onSaved = {})

            // when
            viewModel.onNameChange("my-name")

            // then
            viewModel.uiState.value shouldBe NameUiState(name = "my-name", emptyNameShown = false)
        }
    }

    @Nested
    inner class OnContinue {
        @Test
        fun `should point out an empty name`() {
            // given
            val viewModel = NameViewModel(FakeUserPreferencesRepository())
            viewModel.onNameChange("  ")

            // when
            viewModel.onContinue(onSaved = {})

            // then
            viewModel.uiState.value.emptyNameShown.shouldBeTrue()
        }

        @Test
        fun `should not save an empty name`() {
            // given
            val preferences = FakeUserPreferencesRepository()
            val viewModel = NameViewModel(preferences)
            var saved = false

            // when
            viewModel.onContinue(onSaved = { saved = true })

            // then
            saved.shouldBeFalse()
            preferences.authorName.value.shouldBeNull()
        }

        @Test
        fun `should save the name without the spaces around it`() {
            // given
            val preferences = FakeUserPreferencesRepository()
            val viewModel = NameViewModel(preferences)
            viewModel.onNameChange(" my-name ")

            // when
            viewModel.onContinue(onSaved = {})

            // then
            preferences.authorName.value shouldBe "my-name"
        }

        @Test
        fun `should go on once the name is saved`() {
            // given
            val viewModel = NameViewModel(FakeUserPreferencesRepository())
            viewModel.onNameChange("my-name")
            var saved = false

            // when
            viewModel.onContinue(onSaved = { saved = true })

            // then
            saved.shouldBeTrue()
        }
    }
}
