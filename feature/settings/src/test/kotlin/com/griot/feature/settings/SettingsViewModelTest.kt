package com.griot.feature.settings

import com.griot.core.data.export.FakeMarkdownExporter
import com.griot.core.data.language.FakeAppLanguageRepository
import com.griot.core.data.model.AppLanguage
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.collections.shouldContainExactly
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class SettingsViewModelTest {
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
        fun `should show the language, the name and the lyrics size`() {
            // given
            val appLanguage = FakeAppLanguageRepository(language = AppLanguage.ENGLISH)
            val preferences = FakeUserPreferencesRepository(authorName = "my-author-name", editorFontSize = 22)

            // when
            val viewModel = SettingsViewModel(
                appLanguage = appLanguage,
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
            )

            // then
            viewModel.uiState.value shouldBe SettingsUiState(
                language = AppLanguage.ENGLISH,
                authorName = "my-author-name",
                fontSize = 22,
            )
        }

        @Test
        fun `should show an empty name when none is given`() {
            // given
            val preferences = FakeUserPreferencesRepository(authorName = null)

            // when
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
            )

            // then
            viewModel.uiState.value.authorName shouldBe ""
        }
    }

    @Nested
    inner class OnNameChange {
        @Test
        fun `should save the name without the spaces around it`() {
            // given
            val preferences = FakeUserPreferencesRepository(authorName = "my-author-name-1")
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
            )

            // when
            viewModel.onNameChange("  my-author-name-2 ")

            // then
            preferences.authorName.value shouldBe "my-author-name-2"
            viewModel.uiState.value.authorName shouldBe "my-author-name-2"
        }

        @Test
        fun `should keep the name before when the new one is blank`() {
            // given
            val preferences = FakeUserPreferencesRepository(authorName = "my-author-name")
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
            )

            // when
            viewModel.onNameChange("   ")

            // then
            preferences.authorName.value shouldBe "my-author-name"
        }
    }

    @Nested
    inner class OnLanguageSelected {
        @Test
        fun `should apply the language to the interface`() {
            // given
            val appLanguage = FakeAppLanguageRepository(language = AppLanguage.SYSTEM)
            val viewModel = SettingsViewModel(
                appLanguage = appLanguage,
                preferences = FakeUserPreferencesRepository(),
                exporter = FakeMarkdownExporter(),
            )

            // when
            viewModel.onLanguageSelected(AppLanguage.FRENCH)

            // then
            appLanguage.language shouldBe AppLanguage.FRENCH
            viewModel.uiState.value.language shouldBe AppLanguage.FRENCH
        }

        @ParameterizedTest
        @CsvSource("FRENCH, FRENCH", "ENGLISH, ENGLISH")
        fun `should make the language the one of new songs`(language: AppLanguage, songLanguage: SongLanguage) {
            // given
            val preferences = FakeUserPreferencesRepository(defaultSongLanguage = SongLanguage.AUTOMATIC)
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
            )

            // when
            viewModel.onLanguageSelected(language)

            // then
            preferences.defaultSongLanguage.value shouldBe songLanguage
        }

        @Test
        fun `should keep the language of new songs for the phone language`() {
            // given
            val preferences = FakeUserPreferencesRepository(defaultSongLanguage = SongLanguage.ENGLISH)
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(language = AppLanguage.ENGLISH),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
            )

            // when
            viewModel.onLanguageSelected(AppLanguage.SYSTEM)

            // then
            preferences.defaultSongLanguage.value shouldBe SongLanguage.ENGLISH
        }
    }

    @Nested
    inner class OnExportAll {
        @Test
        fun `should export every song and show the zip`() {
            // given
            val exporter = FakeMarkdownExporter()
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = FakeUserPreferencesRepository(),
                exporter = exporter,
            )

            // when
            viewModel.onExportAll()

            // then
            viewModel.uiState.value.export shouldBe exporter.file
            exporter.calls shouldContainExactly listOf("all")
        }
    }
}
