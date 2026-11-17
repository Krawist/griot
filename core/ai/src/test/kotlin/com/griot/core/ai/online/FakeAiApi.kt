package com.griot.core.ai.online

import com.griot.core.ai.AiPrompt

/** [AiApi] for tests: answers [check] and [answer], and records the prompts it got. */
internal class FakeAiApi(private val check: KeyCheck = KeyCheck.ACCEPTED, private val answer: String = "") : AiApi {
    val prompts = mutableListOf<AiPrompt>()

    override suspend fun check() = check

    override suspend fun generate(prompt: AiPrompt): String {
        prompts += prompt
        return answer
    }
}
