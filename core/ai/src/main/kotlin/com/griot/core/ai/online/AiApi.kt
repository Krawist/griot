package com.griot.core.ai.online

import com.griot.core.ai.AiPrompt

/** An online model's API, called with the author's key, which [AiApiFactory] picks from that key. */
internal interface AiApi {
    /** Whether the provider accepts the key, without spending anything. */
    suspend fun check(): KeyCheck

    /** The model's answer to [prompt], as text; empty when it declines to answer. */
    suspend fun generate(prompt: AiPrompt): String
}
