package com.griot.feature.settings

import com.griot.core.data.language.FakeAppLanguageRepository
import com.griot.core.data.model.AppLanguage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SettingsViewModelTest {
    @Nested
    inner class UiState {
        @Test
        fun `should show the language of the interface`() {
            // given
            val appLanguage = FakeAppLanguageRepository(language = AppLanguage.ENGLISH)

            // when
            val viewModel = SettingsViewModel(appLanguage)

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
            val viewModel = SettingsViewModel(appLanguage)

            // when
            viewModel.onLanguageSelected(AppLanguage.FRENCH)

            // then
            appLanguage.language shouldBe AppLanguage.FRENCH
        }

        @Test
        fun `should show the language selected`() {
            // given
            val appLanguage = FakeAppLanguageRepository(language = AppLanguage.SYSTEM)
            val viewModel = SettingsViewModel(appLanguage)

            // when
            viewModel.onLanguageSelected(AppLanguage.ENGLISH)

            // then
            viewModel.uiState.value shouldBe SettingsUiState(language = AppLanguage.ENGLISH)
        }
    }
}
