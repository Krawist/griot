package com.griot.core.phonetics

/**
 * One word of a verse.
 *
 * @property text The word as written, apostrophes made straight.
 * @property start Index of its first letter in the verse.
 * @property end Index just after its last letter in the verse.
 */
internal data class Word(val text: String, val start: Int, val end: Int)
