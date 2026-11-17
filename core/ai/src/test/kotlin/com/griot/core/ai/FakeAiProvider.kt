package com.griot.core.ai

/** [AiProvider] for tests: answers [answer] when [available], and records the prompts it got. */
internal class FakeAiProvider(
    override val location: AiLocation,
    private val available: Boolean = true,
    private val answer: String = "",
) : AiProvider {
    val prompts = mutableListOf<AiPrompt>()

    override suspend fun isAvailable() = available

    override suspend fun generate(prompt: AiPrompt): String {
        prompts += prompt
        return answer
    }
}
