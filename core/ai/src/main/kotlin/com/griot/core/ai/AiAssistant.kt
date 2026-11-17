package com.griot.core.ai

/**
 * The AI help on a verse: rephrase it or critique it, from the model on the phone when available, online otherwise.
 * Every suggestion is checked by the phonetic engine before it is returned.
 */
interface AiAssistant {
    /** Where the next answer would come from; `null` when no model is available. */
    suspend fun location(): AiLocation?

    /**
     * At most three versions of the verse that keep its meaning, its rhyme and a close syllable count, the others
     * dropped; `null` when no model is available.
     */
    suspend fun rephrase(request: VerseRequest): AiAnswer<CheckedSuggestion>?

    /** At most three short remarks on the verse, its rhythm read from the engine's counts; `null` without a model. */
    suspend fun critique(request: VerseRequest): AiAnswer<String>?
}
