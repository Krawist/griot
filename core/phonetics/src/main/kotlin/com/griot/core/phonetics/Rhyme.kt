package com.griot.core.phonetics

/**
 * The rhyme of a verse: two verses rhyme when their keys are equal.
 *
 * @property key The sound of the verse's ending, from its last vowel to the end, prefixed by its language.
 * @property start Index in the verse of the first letter of the rhyming ending (« ville » → « ille »).
 * @property end Index in the verse just after the last letter of the rhyming ending, punctuation left out.
 */
data class Rhyme(val key: String, val start: Int, val end: Int)
