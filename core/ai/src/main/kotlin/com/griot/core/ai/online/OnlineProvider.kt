package com.griot.core.ai.online

import com.griot.core.ai.AiLocation
import com.griot.core.ai.AiPrompt
import com.griot.core.ai.AiProvider
import com.griot.core.ai.AiStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The online model of the author's key, its API picked by [AiApiFactory]: ready once a key of a known provider is
 * saved. The verses sent leave the phone, which the AI panel tells.
 */
@Singleton
internal class OnlineProvider @Inject constructor(private val keys: ApiKeyRepository, private val apis: AiApiFactory) :
    AiProvider {
    override val location = AiLocation.ONLINE

    override suspend fun status(): AiStatus = if (api() != null) AiStatus.READY else AiStatus.UNAVAILABLE

    override suspend fun generate(prompt: AiPrompt): String {
        val api = checkNotNull(api()) { "No key of a known provider" }
        return api.generate(prompt)
    }

    private suspend fun api(): AiApi? = keys.read()?.let { apis.create(it) }
}
