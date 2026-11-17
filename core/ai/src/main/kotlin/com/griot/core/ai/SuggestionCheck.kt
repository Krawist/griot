package com.griot.core.ai

import com.griot.core.phonetics.PhoneticEngine
import kotlin.math.abs

/** How far a suggestion's count may be from the verse's: « a close syllable count ». */
internal const val SYLLABLE_TOLERANCE = 1

/**
 * Counts every suggestion with [engine] and keeps those that rhyme like the verse of [request] (when it has a rhyme)
 * and whose count is within [SYLLABLE_TOLERANCE] of its own; the verse itself, given back, is dropped.
 */
internal fun checkSuggestions(
    engine: PhoneticEngine,
    request: VerseRequest,
    suggestions: List<String>,
): List<CheckedSuggestion> {
    val rhyme = engine.rhyme(request.verse)?.key
    return suggestions
        .filter { it.trim() != request.verse.trim() }
        .mapNotNull { text ->
            val count = engine.countSyllables(text)
            val rhymes = rhyme == null || engine.rhyme(text)?.key == rhyme
            CheckedSuggestion(text = text, syllables = count.syllables, guessed = count.guessedWords.isNotEmpty())
                .takeIf { rhymes && abs(count.syllables - request.targetSyllables) <= SYLLABLE_TOLERANCE }
        }
}
