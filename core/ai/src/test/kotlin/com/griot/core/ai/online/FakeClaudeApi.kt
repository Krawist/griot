package com.griot.core.ai.online

import com.griot.core.ai.AiPrompt

/** [ClaudeApi] for tests: answers [check] and [answer], and records the keys it got. */
internal class FakeClaudeApi(private val check: KeyCheck = KeyCheck.ACCEPTED, private val answer: String = "") :
    ClaudeApi {
    val keys = mutableListOf<String>()

    override suspend fun check(key: String): KeyCheck {
        keys += key
        return check
    }

    override suspend fun generate(key: String, prompt: AiPrompt): String {
        keys += key
        return answer
    }
}
