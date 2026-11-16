package com.griot.feature.settings

import com.griot.core.data.export.FakeMarkdownExporter
import com.griot.core.data.language.FakeAppLanguageRepository
import com.griot.core.data.model.AppLanguage
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
        fun `should show the language of the interface`() {
            // given
            val appLanguage = FakeAppLanguageRepository(language = AppLanguage.ENGLISH)

            // when
            val viewModel = SettingsViewModel(appLanguage = appLanguage, exporter = FakeMarkdownExporter())

            // then
            viewModel.uiState.value shouldBe SettingsUiState(language = AppLanguage.ENGLISH)
        }
    }

    @Nested
    inner class OnLanguageSelected {
        @Test
        fun `should apply the language to the interface`() {
            // given
            val appLanguage = FakeAppLanguageRepository(language = AppLanguage.SYSTEM)
            val viewModel = SettingsViewModel(appLanguage = appLanguage, exporter = FakeMarkdownExporter())

            // when
            viewModel.onLanguageSelected(AppLanguage.FRENCH)

            // then
            appLanguage.language shouldBe AppLanguage.FRENCH
        }

        @Test
        fun `should show the language selected`() {
            // given
            val appLanguage = FakeAppLanguageRepository(language = AppLanguage.SYSTEM)
            val viewModel = SettingsViewModel(appLanguage = appLanguage, exporter = FakeMarkdownExporter())

            // when
            viewModel.onLanguageSelected(AppLanguage.ENGLISH)

            // then
            viewModel.uiState.value shouldBe SettingsUiState(language = AppLanguage.ENGLISH)
        }
    }

    @Nested
    inner class OnExportAll {
        @Test
        fun `should export every song and show the zip`() {
            // given
            val exporter = FakeMarkdownExporter()
            val viewModel = SettingsViewModel(appLanguage = FakeAppLanguageRepository(), exporter = exporter)

            // when
            viewModel.onExportAll()

            // then
            viewModel.uiState.value.export shouldBe exporter.file
            exporter.calls shouldContainExactly listOf("all")
        }
    }
}
