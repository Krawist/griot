package com.griot.core.ai

/** [AiProvider] for tests: answers [answer] when its [status] is ready, and records the prompts it got. */
internal class FakeAiProvider(
    override val location: AiLocation,
    private val status: AiStatus = AiStatus.READY,
    private val answer: String = "",
) : AiProvider {
    val prompts = mutableListOf<AiPrompt>()

    override suspend fun status() = status

    override suspend fun generate(prompt: AiPrompt): String {
        prompts += prompt
        return answer
    }
}
