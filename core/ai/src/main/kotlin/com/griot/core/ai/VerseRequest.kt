package com.griot.core.ai

import com.griot.core.phonetics.PhoneticLanguage

/**
 * A verse the author asks help on, with what a model needs to respect their song.
 *
 * @property language The language the song is written in.
 * @property section The label of the verse's section, as shown (« Couplet 1 »).
 * @property verse The verse as written.
 * @property before The verses of the section before it, in order.
 * @property after The verses of the section after it, in order.
 * @property targetSyllables The number of syllables to keep: the verse's own count.
 */
data class VerseRequest(
    val language: PhoneticLanguage,
    val section: String,
    val verse: String,
    val before: List<String>,
    val after: List<String>,
    val targetSyllables: Int,
)
