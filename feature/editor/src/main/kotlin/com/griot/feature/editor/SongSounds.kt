package com.griot.feature.editor

import com.griot.core.data.model.SongVerse
import com.griot.core.phonetics.PhoneticEngine
import com.griot.core.phonetics.rhymeLetters
import com.griot.core.phonetics.rhymeScheme

/**
 * How the [verses] of a section sound with [engine]: their syllables, and their rhyme letters given in order of
 * appearance in the section (A, B, C…). A verse rhymes when another verse of the section has its rhyme.
 */
internal fun sectionSounds(verses: List<SongVerse>, engine: PhoneticEngine): SectionSounds {
    val rhymes = verses.map { engine.rhyme(it.text) }
    val letters = rhymeLetters(rhymes)
    val shared = rhymes.mapNotNull { it?.key }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    val sounds = verses.withIndex().associate { (index, verse) ->
        val rhyme = rhymes[index]
        verse.id to VerseSound(
            count = engine.countSyllables(verse.text),
            letter = letters[index],
            rhymes = rhyme != null && rhyme.key in shared,
            ending = rhyme?.let { it.start until it.end },
        )
    }
    return SectionSounds(verses = sounds, scheme = rhymeScheme(letters))
}
