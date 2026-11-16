package com.griot.core.phonetics

/**
 * The number of syllables of a verse, as sung.
 *
 * @property syllables The count.
 * @property guessed Whether a word of the verse is in no dictionary: its syllables come from grapheme rules, so the
 * count is shown lighter.
 */
data class SyllableCount(val syllables: Int, val guessed: Boolean)
