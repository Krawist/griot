package com.griot.core.ai

/**
 * A model that answers a prompt: Gemini Nano on the phone (GRI-32) or a small online model (GRI-33). Prompts,
 * parsing and checks are shared in [AiAssistant]: a provider only runs its model.
 */
interface AiProvider {
    /** Where the model runs. */
    val location: AiLocation

    /** Whether the model can answer now: supported by the phone and downloaded, or a key is set. */
    suspend fun isAvailable(): Boolean

    /** The model's answer to [prompt], as text. */
    suspend fun generate(prompt: AiPrompt): String
}
