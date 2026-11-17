package com.griot.core.ai.online

import com.griot.core.ai.AiLocation
import com.griot.core.ai.AiPrompt
import com.griot.core.ai.AiProvider
import com.griot.core.ai.AiStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Claude, online, with the author's key: ready once a key is saved. The verses sent leave the phone for Anthropic,
 * which the AI panel tells.
 */
@Singleton
internal class OnlineProvider @Inject constructor(private val keys: ApiKeyRepository, private val api: ClaudeApi) :
    AiProvider {
    override val location = AiLocation.ONLINE

    override suspend fun status(): AiStatus = if (keys.read() != null) AiStatus.READY else AiStatus.UNAVAILABLE

    override suspend fun generate(prompt: AiPrompt): String {
        val key = checkNotNull(keys.read()) { "No key for the online model" }
        return api.generate(key = key, prompt = prompt)
    }
}
