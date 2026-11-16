package com.griot.core.phonetics

/**
 * The number of syllables of a verse, as sung.
 *
 * @property syllables The count.
 * @property guessedWords The words of the verse in no dictionary, nor corrected by the user, in order: their
 * syllables come from grapheme rules, so the count is shown lighter. Empty when every word is known.
 */
data class SyllableCount(val syllables: Int, val guessedWords: List<String> = emptyList())
