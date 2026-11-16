package com.griot.feature.editor

import com.griot.core.phonetics.SyllableCount

/**
 * How a verse sounds, as the editor shows it.
 *
 * @property count Its syllables, lighter when a word was guessed.
 * @property letter Rank of its rhyme in its section, in order of appearance (0 for A); `null` for an empty verse.
 * @property rhymes Whether another verse of its section shares its rhyme: only then is it coloured.
 * @property ending Range of the rhyming ending in the verse's text, highlighted when it [rhymes]; `null` without one.
 */
internal data class VerseSound(val count: SyllableCount, val letter: Int?, val rhymes: Boolean, val ending: IntRange?)
