package com.griot.core.ai

/**
 * [AiProvider] for tests: answers [answer] when its [status] is ready, or throws [failure] when given, and records the
 * prompts it got.
 */
internal class FakeAiProvider(
    override val location: AiLocation,
    private val status: AiStatus = AiStatus.READY,
    private val answer: String = "",
    private val failure: Exception? = null,
) : AiProvider {
    val prompts = mutableListOf<AiPrompt>()

    override suspend fun status() = status

    override suspend fun generate(prompt: AiPrompt): String {
        prompts += prompt
        failure?.let { throw it }
        return answer
    }
}
