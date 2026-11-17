package com.griot.feature.settings

import com.griot.core.ai.AiStatus
import com.griot.core.ai.FakeAiAssistant
import com.griot.core.ai.online.FakeApiKeyRepository
import com.griot.core.ai.online.KeyCheck
import com.griot.core.data.audio.FakeExternalMusic
import com.griot.core.data.export.FakeMarkdownExporter
import com.griot.core.data.language.FakeAppLanguageRepository
import com.griot.core.data.model.AppLanguage
import com.griot.core.data.model.PreferredAi
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.ThemeMode
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
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
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
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
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
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
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
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
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
            )

            // when
            viewModel.onLanguageSelected(AppLanguage.FRENCH)

            // then
            appLanguage.language shouldBe AppLanguage.FRENCH
            viewModel.uiState.value.language shouldBe AppLanguage.FRENCH
        }

        @ParameterizedTest
        @CsvSource("FRENCH, ENGLISH, FRENCH", "ENGLISH, FRENCH, ENGLISH")
        fun `should make the language the one of new songs`(
            language: AppLanguage,
            before: SongLanguage,
            songLanguage: SongLanguage,
        ) {
            // given
            val preferences = FakeUserPreferencesRepository(defaultSongLanguage = before)
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
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
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
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
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
            )

            // when
            viewModel.onExportAll()

            // then
            viewModel.uiState.value.export shouldBe exporter.file
            exporter.calls shouldContainExactly listOf("all")
        }
    }

    @Nested
    inner class OnDeviceAi {
        @Test
        fun `should show whether the model of the phone can answer`() = runTest {
            // when
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = FakeUserPreferencesRepository(onDeviceAi = false),
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(deviceStatus = AiStatus.PREPARING),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
            )

            // then
            viewModel.uiState.value.onDeviceStatus shouldBe AiStatus.PREPARING
            viewModel.uiState.value.onDeviceAi shouldBe false
        }

        @Test
        fun `should save whether the AI help may use the model of the phone`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository(onDeviceAi = true)
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(deviceStatus = AiStatus.READY),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
            )

            // when
            viewModel.onOnDeviceAiChange(false)

            // then
            preferences.onDeviceAi.value shouldBe false
        }
    }

    @Nested
    inner class OnOnlineAiChange {
        @Test
        fun `should save whether the AI help may use the online model`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository(onlineAi = true)
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(key = "my-key"),
            )

            // when
            viewModel.onOnlineAiChange(false)

            // then
            preferences.onlineAi.value shouldBe false
        }
    }

    @Nested
    inner class UsableAi {
        @Test
        fun `should offer both AIs when the phone has its model and a key is saved`() {
            // when
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = FakeUserPreferencesRepository(),
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(deviceStatus = AiStatus.READY),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(key = "my-key"),
            )

            // then
            viewModel.uiState.value.usableAi shouldContainExactly listOf(PreferredAi.DEVICE, PreferredAi.ONLINE)
        }

        @Test
        fun `should offer only the online AI when the phone cannot have its model`() {
            // when
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = FakeUserPreferencesRepository(),
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(deviceStatus = AiStatus.UNAVAILABLE),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(key = "my-key"),
            )

            // then
            viewModel.uiState.value.usableAi shouldContainExactly listOf(PreferredAi.ONLINE)
        }

        @Test
        fun `should leave out an AI the settings forbid`() {
            // when
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = FakeUserPreferencesRepository(onlineAi = false),
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(deviceStatus = AiStatus.READY),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(key = "my-key"),
            )

            // then
            viewModel.uiState.value.usableAi shouldContainExactly listOf(PreferredAi.DEVICE)
        }

        @Test
        fun `should offer none without a model or a key`() {
            // when
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = FakeUserPreferencesRepository(),
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(deviceStatus = AiStatus.UNAVAILABLE),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
            )

            // then
            viewModel.uiState.value.usableAi shouldBe emptyList()
        }
    }

    @Nested
    inner class OnPreferredAiChange {
        @Test
        fun `should save the model the AI help asks first`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
            )

            // when
            viewModel.onPreferredAiChange(PreferredAi.ONLINE)

            // then
            preferences.preferredAi.value shouldBe PreferredAi.ONLINE
            viewModel.uiState.value.preferredAi shouldBe PreferredAi.ONLINE
        }
    }

    @Nested
    inner class OnApiKeySave {
        @Test
        fun `should show the key as saved once Anthropic accepts it`() = runTest {
            // given
            val keys = FakeApiKeyRepository()
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = FakeUserPreferencesRepository(),
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = keys,
            )

            // when
            viewModel.onApiKeySave("my-key")

            // then
            viewModel.uiState.value.apiKeySet shouldBe true
            viewModel.uiState.value.keyCheck shouldBe KeyCheck.ACCEPTED
            keys.key.value shouldBe "my-key"
        }

        @Test
        fun `should tell when Anthropic refuses the key`() = runTest {
            // given
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = FakeUserPreferencesRepository(),
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(check = KeyCheck.REFUSED),
            )

            // when
            viewModel.onApiKeySave("my-key")

            // then
            viewModel.uiState.value.apiKeySet shouldBe false
            viewModel.uiState.value.keyCheck shouldBe KeyCheck.REFUSED
        }
    }

    @Nested
    inner class OnApiKeyClear {
        @Test
        fun `should forget the key`() = runTest {
            // given
            val keys = FakeApiKeyRepository(key = "my-key")
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = FakeUserPreferencesRepository(),
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = keys,
            )

            // when
            viewModel.onApiKeyClear()

            // then
            keys.key.value shouldBe null
            viewModel.uiState.value.apiKeySet shouldBe false
        }
    }

    @Nested
    inner class OnThemeModeChange {
        @Test
        fun `should save and show the theme chosen`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
            )

            // when
            viewModel.onThemeModeChange(ThemeMode.DARK)

            // then
            preferences.themeMode.value shouldBe ThemeMode.DARK
            viewModel.uiState.value.themeMode shouldBe ThemeMode.DARK
        }
    }

    @Nested
    inner class OnFontSizeChange {
        @Test
        fun `should save and show the size of the lyrics`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = preferences,
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(),
                apiKeys = FakeApiKeyRepository(),
            )

            // when
            viewModel.onFontSizeChange(24)

            // then
            preferences.editorFontSize.value shouldBe 24
            viewModel.uiState.value.fontSize shouldBe 24
        }
    }

    @Nested
    inner class OnResume {
        @Test
        fun `should read whether other apps' music is controlled`() {
            // given
            val viewModel = SettingsViewModel(
                appLanguage = FakeAppLanguageRepository(),
                preferences = FakeUserPreferencesRepository(),
                exporter = FakeMarkdownExporter(),
                assistant = FakeAiAssistant(),
                externalMusic = FakeExternalMusic(access = true),
                apiKeys = FakeApiKeyRepository(),
            )

            // when
            viewModel.onResume()

            // then
            viewModel.uiState.value.externalMusic shouldBe true
        }
    }
}
