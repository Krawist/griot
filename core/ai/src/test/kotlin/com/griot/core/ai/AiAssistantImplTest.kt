package com.griot.core.ai

import com.griot.core.data.preferences.FakeUserPreferencesRepository
import com.griot.core.phonetics.PhoneticEngines
import com.griot.core.phonetics.PhoneticLanguage
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AiAssistantImplTest {
    private val engines = PhoneticEngines()

    @Nested
    inner class Availability {
        @Test
        fun `should prefer the model of the phone`() = runTest {
            // given
            val assistant = AiAssistantImpl(
                providers = setOf(FakeAiProvider(AiLocation.ONLINE), FakeAiProvider(AiLocation.DEVICE)),
                engines = engines,
                preferences = FakeUserPreferencesRepository(),
            )

            // when
            val availability = assistant.availability()

            // then
            availability shouldBe AiAvailability(location = AiLocation.DEVICE, preparing = false)
        }

        @Test
        fun `should go online while the model of the phone downloads`() = runTest {
            // given
            val assistant = AiAssistantImpl(
                providers = setOf(
                    FakeAiProvider(location = AiLocation.DEVICE, status = AiStatus.PREPARING),
                    FakeAiProvider(location = AiLocation.ONLINE),
                ),
                engines = engines,
                preferences = FakeUserPreferencesRepository(),
            )

            // when
            val availability = assistant.availability()

            // then
            availability shouldBe AiAvailability(location = AiLocation.ONLINE, preparing = true)
        }

        @Test
        fun `should leave out the model of the phone when the settings forbid it`() = runTest {
            // given
            val assistant = AiAssistantImpl(
                providers = setOf(FakeAiProvider(AiLocation.DEVICE)),
                engines = engines,
                preferences = FakeUserPreferencesRepository(onDeviceAi = false),
            )

            // when
            val availability = assistant.availability()

            // then
            availability shouldBe AiAvailability(location = null, preparing = false)
        }

        @Test
        fun `should leave out the online model when the settings forbid it`() = runTest {
            // given
            val assistant = AiAssistantImpl(
                providers = setOf(FakeAiProvider(AiLocation.ONLINE)),
                engines = engines,
                preferences = FakeUserPreferencesRepository(onlineAi = false),
            )

            // when
            val availability = assistant.availability()

            // then
            availability shouldBe AiAvailability(location = null, preparing = false)
        }

        @Test
        fun `should have none without a model`() = runTest {
            // given
            val assistant = AiAssistantImpl(
                providers = emptySet(),
                engines = engines,
                preferences = FakeUserPreferencesRepository(),
            )

            // when
            val availability = assistant.availability()

            // then
            availability shouldBe AiAvailability(location = null, preparing = false)
        }
    }

    @Nested
    inner class Status {
        @Test
        fun `should give the status of the model of the phone, whatever the settings`() = runTest {
            // given
            val assistant = AiAssistantImpl(
                providers = setOf(FakeAiProvider(location = AiLocation.DEVICE, status = AiStatus.PREPARING)),
                engines = engines,
                preferences = FakeUserPreferencesRepository(onDeviceAi = false),
            )

            // when
            val status = assistant.status(AiLocation.DEVICE)

            // then
            status shouldBe AiStatus.PREPARING
        }

        @Test
        fun `should be unavailable without a model there`() = runTest {
            // given
            val assistant = AiAssistantImpl(
                providers = setOf(FakeAiProvider(AiLocation.DEVICE)),
                engines = engines,
                preferences = FakeUserPreferencesRepository(),
            )

            // when
            val status = assistant.status(AiLocation.ONLINE)

            // then
            status shouldBe AiStatus.UNAVAILABLE
        }
    }

    @Nested
    inner class Rephrase {
        @Test
        fun `should give the suggestions checked by the phonetic engine`() = runTest {
            // given
            val provider = FakeAiProvider(
                location = AiLocation.DEVICE,
                answer = "1. Le soleil frappe sur la tôle\n2. Le soleil frappe sur les toits",
            )
            val assistant = AiAssistantImpl(
                providers = setOf(provider),
                engines = engines,
                preferences = FakeUserPreferencesRepository(),
            )
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "Couplet 1",
                verse = "Le soleil cogne sur la tôle",
                before = emptyList(),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val answer = assistant.rephrase(request)

            // then
            answer shouldBe AiAnswer(
                items = listOf(
                    CheckedSuggestion(text = "Le soleil frappe sur la tôle", syllables = 8, guessed = false),
                ),
                location = AiLocation.DEVICE,
            )
            provider.prompts shouldContainExactly listOf(rephrasePrompt(request))
        }

        @Test
        fun `should give nothing without a model`() = runTest {
            // given
            val assistant = AiAssistantImpl(
                providers = emptySet(),
                engines = engines,
                preferences = FakeUserPreferencesRepository(),
            )
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "Couplet 1",
                verse = "Le soleil cogne sur la tôle",
                before = emptyList(),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val answer = assistant.rephrase(request)

            // then
            answer.shouldBeNull()
        }
    }

    @Nested
    inner class Critique {
        @Test
        fun `should give the counts of the section to the model`() = runTest {
            // given
            val provider = FakeAiProvider(location = AiLocation.ONLINE, answer = "my-remark")
            val assistant = AiAssistantImpl(
                providers = setOf(provider),
                engines = engines,
                preferences = FakeUserPreferencesRepository(),
            )
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "Couplet 1",
                verse = "Le soleil cogne sur la tôle",
                before = listOf("La poussière danse sur nos vies."),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val answer = assistant.critique(request)

            // then
            answer shouldBe AiAnswer(items = listOf("my-remark"), location = AiLocation.ONLINE)
            provider.prompts.single().request shouldContain "Syllables of the verses of the section, in order: 9, 8"
        }

        @Test
        fun `should give nothing without a model`() = runTest {
            // given
            val assistant = AiAssistantImpl(
                providers = emptySet(),
                engines = engines,
                preferences = FakeUserPreferencesRepository(),
            )
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "Couplet 1",
                verse = "Le soleil cogne sur la tôle",
                before = emptyList(),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val answer = assistant.critique(request)

            // then
            answer.shouldBeNull()
        }
    }
}
