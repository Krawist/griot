package com.griot.core.ai.online

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AiApiFactoryImplTest {
    @Nested
    inner class Create {
        @Test
        fun `should open Anthropic's API with an Anthropic key`() {
            // given
            val factory = AiApiFactoryImpl()

            // when
            val api = factory.create("sk-ant-my-key")

            // then
            api.shouldBeInstanceOf<ClaudeApi>()
        }

        @Test
        fun `should keep the API while the key does not change`() {
            // given
            val factory = AiApiFactoryImpl()
            val first = factory.create("sk-ant-my-key")

            // when
            val second = factory.create("sk-ant-my-key")

            // then
            second shouldBeSameInstanceAs first
        }

        @Test
        fun `should open nothing with a key of no provider Griot knows`() {
            // given
            val factory = AiApiFactoryImpl()

            // when
            val api = factory.create("my-key")

            // then
            api.shouldBeNull()
        }
    }
}
