package com.griot.core.ai

/**
 * What a model is asked.
 *
 * @property instructions The rules of the answer: its language, its form, what never to do.
 * @property request The verse, its place in the song and what is asked about it.
 */
data class AiPrompt(val instructions: String, val request: String)
