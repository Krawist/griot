package com.griot.core.ai

/**
 * A verse proposed by a model, once counted by the phonetic engine: the count shown is never the model's.
 *
 * @property text The verse proposed.
 * @property syllables Its number of syllables, from the phonetic engine.
 * @property guessed Whether a word of it is in no dictionary, so that the count is shown lighter.
 */
data class CheckedSuggestion(val text: String, val syllables: Int, val guessed: Boolean)
