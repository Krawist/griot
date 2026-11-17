package com.griot.core.ai.online

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation for [AiApiFactory]: the provider is told by the key's prefix, Anthropic's `sk-ant-` only for
 * now. The last API is kept while its key does not change, with its HTTP client.
 */
@Singleton
internal class AiApiFactoryImpl @Inject constructor() : AiApiFactory {
    private var last: Pair<String, AiApi>? = null

    @Synchronized
    override fun create(key: String): AiApi? {
        last?.let { (lastKey, api) -> if (lastKey == key) return api }
        val api = when {
            key.startsWith(ANTHROPIC_PREFIX) -> ClaudeApi(key)
            else -> null
        }
        (last?.second as? AutoCloseable)?.close()
        last = api?.let { key to it }
        return api
    }

    private companion object {
        const val ANTHROPIC_PREFIX = "sk-ant-"
    }
}
