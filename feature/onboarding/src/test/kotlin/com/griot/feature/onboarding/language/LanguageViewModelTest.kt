package com.griot.feature.onboarding.language

import com.griot.core.data.language.FakeAppLanguageRepository
import com.griot.core.data.model.AppLanguage
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.shouldBe
import java.util.Locale
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
import org.junit.jupiter.params.provider.EnumSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class LanguageViewModelTest {
    private val phoneLocale = Locale.getDefault()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        Locale.setDefault(phoneLocale)
    }

    @Nested
    inner class UiState {
        @ParameterizedTest
        @CsvSource("fr-FR, FRENCH", "fr-CM, FRENCH", "en-GB, ENGLISH", "de-DE, ENGLISH")
        fun `should preselect the phone's language, English when it is neither`(tag: String, expected: AppLanguage) {
            // given
            Locale.setDefault(Locale.forLanguageTag(tag))

            // when
            val viewModel = LanguageViewModel(
                appLanguage = FakeAppLanguageRepository(language = AppLanguage.SYSTEM),
                preferences = FakeUserPreferencesRepository(),
            )

            // then
            viewModel.uiState.value shouldBe LanguageUiState(selected = expected)
        }

        @ParameterizedTest
        @EnumSource(AppLanguage::class, names = ["SYSTEM"], mode = EnumSource.Mode.EXCLUDE)
        fun `should preselect the language picked before`(language: AppLanguage) {
            // given
            Locale.setDefault(Locale.forLanguageTag("de-DE"))

            // when
            val viewModel = LanguageViewModel(
                appLanguage = FakeAppLanguageRepository(language = language),
                preferences = FakeUserPreferencesRepository(),
            )

            // then
            viewModel.uiState.value shouldBe LanguageUiState(selected = language)
        }
    }

    @Nested
    inner class OnLanguageSelected {
        @Test
        fun `should select the language`() {
            // given
            val viewModel = LanguageViewModel(
                appLanguage = FakeAppLanguageRepository(language = AppLanguage.FRENCH),
                preferences = FakeUserPreferencesRepository(),
            )

            // when
            viewModel.onLanguageSelected(AppLanguage.ENGLISH)

            // then
            viewModel.uiState.value shouldBe LanguageUiState(selected = AppLanguage.ENGLISH)
        }
    }

    @Nested
    inner class OnContinue {
        @ParameterizedTest
        @CsvSource("FRENCH, FRENCH", "ENGLISH, ENGLISH")
        fun `should write new songs in the language picked`(picked: AppLanguage, expected: SongLanguage) {
            // given
            val preferences = FakeUserPreferencesRepository()
            val viewModel = LanguageViewModel(
                appLanguage = FakeAppLanguageRepository(language = picked),
                preferences = preferences,
            )

            // when
            viewModel.onContinue(onSaved = {})

            // then
            preferences.defaultSongLanguage.value shouldBe expected
        }

        @Test
        fun `should apply the language picked to the interface`() {
            // given
            val appLanguage = FakeAppLanguageRepository(language = AppLanguage.FRENCH)
            val viewModel = LanguageViewModel(appLanguage = appLanguage, preferences = FakeUserPreferencesRepository())
            viewModel.onLanguageSelected(AppLanguage.ENGLISH)

            // when
            viewModel.onContinue(onSaved = {})

            // then
            appLanguage.language shouldBe AppLanguage.ENGLISH
        }

        @Test
        fun `should go on before the interface is redrawn in the new language`() {
            // given
            val appLanguage = FakeAppLanguageRepository(language = AppLanguage.FRENCH)
            val viewModel = LanguageViewModel(appLanguage = appLanguage, preferences = FakeUserPreferencesRepository())
            viewModel.onLanguageSelected(AppLanguage.ENGLISH)
            var languageWhenSaved: AppLanguage? = null

            // when
            viewModel.onContinue(onSaved = { languageWhenSaved = appLanguage.language })

            // then
            languageWhenSaved shouldBe AppLanguage.FRENCH
        }
    }
}
