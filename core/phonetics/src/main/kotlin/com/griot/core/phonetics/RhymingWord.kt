package com.griot.core.phonetics

/**
 * A word of the dictionary that rhymes with a verse.
 *
 * @property word The word, in lower case.
 * @property syllables Its syllables, counted as in a verse (a final mute e left out).
 */
data class RhymingWord(val word: String, val syllables: Int)
