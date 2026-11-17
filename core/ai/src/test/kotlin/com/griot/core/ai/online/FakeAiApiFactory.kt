package com.griot.core.ai.online

/** [AiApiFactory] for tests: gives [api] for every key, and records the keys it got. */
internal class FakeAiApiFactory(private val api: AiApi?) : AiApiFactory {
    val keys = mutableListOf<String>()

    override fun create(key: String): AiApi? {
        keys += key
        return api
    }
}
