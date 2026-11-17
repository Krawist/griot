package com.griot.core.ai.online

import com.griot.core.ai.AiPrompt
import com.griot.core.ai.AiStatus
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class OnlineProviderTest {
    @Nested
    inner class Status {
        @Test
        fun `should be ready once a key is saved`() = runTest {
            // given
            val provider =
                OnlineProvider(keys = FakeApiKeyRepository(key = "my-key"), apis = FakeAiApiFactory(FakeAiApi()))

            // when
            val status = provider.status()

            // then
            status shouldBe AiStatus.READY
        }

        @Test
        fun `should be unavailable with a key of no provider Griot knows`() = runTest {
            // given
            val provider =
                OnlineProvider(keys = FakeApiKeyRepository(key = "my-key"), apis = FakeAiApiFactory(api = null))

            // when
            val status = provider.status()

            // then
            status shouldBe AiStatus.UNAVAILABLE
        }

        @Test
        fun `should be unavailable without a key`() = runTest {
            // given
            val provider = OnlineProvider(keys = FakeApiKeyRepository(), apis = FakeAiApiFactory(FakeAiApi()))

            // when
            val status = provider.status()

            // then
            status shouldBe AiStatus.UNAVAILABLE
        }
    }

    @Nested
    inner class Generate {
        @Test
        fun `should ask Claude with the saved key`() = runTest {
            // given
            val api = FakeAiApi(answer = "my-answer")
            val apis = FakeAiApiFactory(api)
            val provider = OnlineProvider(keys = FakeApiKeyRepository(key = "my-key"), apis = apis)

            // when
            val answer = provider.generate(AiPrompt(instructions = "my-instructions", request = "my-request"))

            // then
            answer shouldBe "my-answer"
            apis.keys shouldContainExactly listOf("my-key")
            api.prompts shouldContainExactly listOf(AiPrompt(instructions = "my-instructions", request = "my-request"))
        }
    }
}
