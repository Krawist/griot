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
            val provider = OnlineProvider(keys = FakeApiKeyRepository(key = "my-key"), api = FakeClaudeApi())

            // when
            val status = provider.status()

            // then
            status shouldBe AiStatus.READY
        }

        @Test
        fun `should be unavailable without a key`() = runTest {
            // given
            val provider = OnlineProvider(keys = FakeApiKeyRepository(), api = FakeClaudeApi())

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
            val api = FakeClaudeApi(answer = "my-answer")
            val provider = OnlineProvider(keys = FakeApiKeyRepository(key = "my-key"), api = api)

            // when
            val answer = provider.generate(AiPrompt(instructions = "my-instructions", request = "my-request"))

            // then
            answer shouldBe "my-answer"
            api.keys shouldContainExactly listOf("my-key")
        }
    }
}
