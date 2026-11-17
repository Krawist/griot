package com.griot.core.phonetics

/**
 * The rhyme of a verse: two verses rhyme when their keys are equal, whatever their language.
 *
 * @property key The sound of the verse's ending, from its last vowel to the end, in Lexique's alphabet for both
 *   languages (an English sound French has not keeps its own letter), close consonants written alike; letters
 *   prefixed by `en~` for a guessed English word.
 * @property start Index in the verse of the first letter of the rhyming ending (« ville » → « ille »).
 * @property end Index in the verse just after the last letter of the rhyming ending, punctuation left out.
 */
data class Rhyme(val key: String, val start: Int, val end: Int)
