package com.griot.core.ai.online

import com.griot.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class ApiKeyRepositoryImplTest {
    @Nested
    inner class Save {
        @Test
        fun `should save the key encrypted once Anthropic accepts it`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            val api = FakeClaudeApi()
            val repository = ApiKeyRepositoryImpl(preferences = preferences, cipher = FakeSecretCipher(), api = api)

            // when
            val check = repository.save(" my-key ")

            // then
            check shouldBe KeyCheck.ACCEPTED
            preferences.onlineAiKey.value shouldBe "encrypted:my-key"
            api.keys shouldContainExactly listOf("my-key")
        }

        @ParameterizedTest
        @EnumSource(KeyCheck::class, names = ["ACCEPTED"], mode = EnumSource.Mode.EXCLUDE)
        fun `should save nothing when Anthropic does not accept the key`(check: KeyCheck) = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            val repository = ApiKeyRepositoryImpl(
                preferences = preferences,
                cipher = FakeSecretCipher(),
                api = FakeClaudeApi(check = check),
            )

            // when
            val result = repository.save("my-key")

            // then
            result shouldBe check
            preferences.onlineAiKey.value.shouldBeNull()
        }
    }

    @Nested
    inner class Read {
        @Test
        fun `should give the saved key decrypted`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            preferences.setOnlineAiKey("encrypted:my-key")
            val repository =
                ApiKeyRepositoryImpl(preferences = preferences, cipher = FakeSecretCipher(), api = FakeClaudeApi())

            // when
            val key = repository.read()

            // then
            key shouldBe "my-key"
        }

        @Test
        fun `should give no key before one is saved`() = runTest {
            // given
            val repository = ApiKeyRepositoryImpl(
                preferences = FakeUserPreferencesRepository(),
                cipher = FakeSecretCipher(),
                api = FakeClaudeApi(),
            )

            // when
            val key = repository.read()

            // then
            key.shouldBeNull()
        }
    }

    @Nested
    inner class Clear {
        @Test
        fun `should forget the saved key`() = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            preferences.setOnlineAiKey("encrypted:my-key")
            val repository =
                ApiKeyRepositoryImpl(preferences = preferences, cipher = FakeSecretCipher(), api = FakeClaudeApi())

            // when
            repository.clear()

            // then
            repository.isSet.first() shouldBe false
        }
    }
}
