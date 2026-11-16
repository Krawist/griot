package com.griot.feature.editor

import com.griot.core.data.model.SongVerse
import com.griot.core.phonetics.PhoneticEngine

/**
 * How the [verses] of a section sound with [engine]: their syllables, and their rhyme letters given in order of
 * appearance in the section (A, B, C…). A verse rhymes when another verse of the section has its rhyme.
 */
internal fun sectionSounds(verses: List<SongVerse>, engine: PhoneticEngine): SectionSounds {
    val rhymes = verses.associate { it.id to engine.rhyme(it.text) }
    val keys = rhymes.values.mapNotNull { it?.key }
    val letters = keys.distinct().withIndex().associate { (index, key) -> key to index }
    val shared = keys.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    val sounds = verses.associate { verse ->
        val rhyme = rhymes[verse.id]
        verse.id to VerseSound(
            count = engine.countSyllables(verse.text),
            letter = rhyme?.let { letters[it.key] },
            rhymes = rhyme != null && rhyme.key in shared,
            ending = rhyme?.let { it.start until it.end },
        )
    }
    val scheme = verses.mapNotNull { sounds[it.id]?.letter }.joinToString(separator = "") { letter(it).toString() }
    return SectionSounds(verses = sounds, scheme = scheme)
}

/** The letter of a rhyme's rank: A for 0, B for 1…, back to A after Z. */
internal fun letter(rank: Int): Char = 'A' + rank % ALPHABET

private const val ALPHABET = 26
