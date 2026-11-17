package com.griot.core.ai.online

import com.griot.core.ai.AiPrompt

/** The Claude API of Anthropic, called with the author's key. */
internal interface ClaudeApi {
    /** Whether Anthropic accepts [key], without spending anything. */
    suspend fun check(key: String): KeyCheck

    /** The model's answer to [prompt], as text; empty when it declines to answer. */
    suspend fun generate(key: String, prompt: AiPrompt): String
}
