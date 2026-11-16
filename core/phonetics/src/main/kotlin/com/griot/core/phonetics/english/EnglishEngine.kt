package com.griot.core.phonetics.english

import com.griot.core.phonetics.PhoneticDictionary
import com.griot.core.phonetics.PhoneticEngine
import com.griot.core.phonetics.PhoneticLanguage
import com.griot.core.phonetics.Rhyme
import com.griot.core.phonetics.SyllableCount
import com.griot.core.phonetics.Transcription
import com.griot.core.phonetics.rhymeStart
import com.griot.core.phonetics.vowelGroups
import com.griot.core.phonetics.wordsOf
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * The English engine, on the CMU Pronouncing Dictionary. Syllables are its vowel phonemes; a rhyme goes from the last
 * stressed vowel to the end of the word.
 *
 * @param other The French engine, for French words of an English song.
 */
class EnglishEngine internal constructor(
    private val dictionary: PhoneticDictionary,
    private val other: () -> PhoneticEngine?,
) : PhoneticEngine {
    /**
     * What a word brings to a verse.
     *
     * @property phonemes Its phonemes, in ARPAbet with stress (empty for a guess).
     * @property syllables Its syllables.
     * @property rhyme Its rhyming ending, from the last stressed vowel, without stress marks.
     * @property silent Letters at its end that are not heard (a silent e), for the rhyme's highlight.
     * @property known Whether a dictionary has it.
     * @property language The engine that read it.
     */
    private data class Analysis(
        val phonemes: List<String>,
        val syllables: Int,
        val rhyme: String,
        val silent: Int,
        val known: Boolean,
        val language: PhoneticLanguage,
    )

    private val cache = ConcurrentHashMap<String, Analysis>()

    override val language = PhoneticLanguage.ENGLISH

    override fun lookup(word: String): Transcription? = dictionary[word.lowercase(Locale.ENGLISH)]?.let { phonemes ->
        Transcription(phonemes = phonemes.split(' '), language = language, known = true)
    }

    override fun transcribe(word: String): Transcription {
        val analysis = analyse(word)
        return Transcription(phonemes = analysis.phonemes, language = analysis.language, known = analysis.known)
    }

    override fun countSyllables(verse: String): SyllableCount {
        val words = wordsOf(verse).map { analyse(it.text) }
        return SyllableCount(syllables = words.sumOf { it.syllables }, guessed = words.any { !it.known })
    }

    override fun rhyme(verse: String): Rhyme? {
        val last = wordsOf(verse).lastOrNull() ?: return null
        val analysis = analyse(last.text)
        // A French word ends the verse: its rhyme is the French engine's.
        return if (analysis.language == PhoneticLanguage.FRENCH) {
            other()?.rhyme(verse)
        } else {
            Rhyme(
                key = (if (analysis.known) "en:" else "en~") + analysis.rhyme,
                start = last.start + rhymeStart(last.text, analysis.silent),
                end = last.end,
            )
        }
    }

    private fun analyse(word: String): Analysis = cache.getOrPut(word.lowercase(Locale.ENGLISH)) {
        val lower = word.lowercase(Locale.ENGLISH)
        dictionary[lower]?.let { known(lower, it.split(' ')) }
            ?: compound(lower)
            ?: foreign(lower)
            ?: guess(lower)
    }

    private fun known(word: String, phonemes: List<String>): Analysis {
        val vowels = phonemes.indices.filter { phonemes[it].last().isDigit() }
        // The last stressed vowel, else the last vowel.
        val from = vowels.lastOrNull { phonemes[it].last() != '0' } ?: vowels.lastOrNull() ?: 0
        return Analysis(
            phonemes = phonemes,
            syllables = vowels.size,
            rhyme = phonemes.drop(from).joinToString(separator = " ") { it.trimEnd { char -> char.isDigit() } },
            silent = if (word.endsWith("e") && vowelGroups(word).size > 1 && !word.endsWith("ee")) 1 else 0,
            known = true,
            language = language,
        )
    }

    // « well-known »: its parts.
    private fun compound(word: String): Analysis? {
        if ('-' !in word) return null
        val parts = word.split('-').filter { it.isNotEmpty() }.map { analyse(it) }
        return parts.last().copy(
            phonemes = parts.flatMap { it.phonemes },
            syllables = parts.sumOf { it.syllables },
            known = parts.all { it.known },
        )
    }

    // A French word in an English song, counted by the French engine.
    private fun foreign(word: String): Analysis? {
        val french = other() ?: return null
        return french.lookup(word)?.let { transcription ->
            Analysis(
                phonemes = transcription.phonemes,
                syllables = french.countSyllables(word).syllables,
                rhyme = "",
                silent = 0,
                known = true,
                language = PhoneticLanguage.FRENCH,
            )
        }
    }

    // A word missing from the dictionary: its groups of vowel letters, but a silent final e (« time »).
    private fun guess(word: String): Analysis {
        val groups = vowelGroups(word)
        val silentE = groups.size > 1 && word.endsWith("e") && !word.endsWith("le") && !word.endsWith("ee")
        val sounded = word.dropLast(if (silentE) 1 else 0)
        val lastGroup = vowelGroups(sounded).lastOrNull()?.first ?: 0
        return Analysis(
            phonemes = emptyList(),
            syllables = (groups.size - if (silentE) 1 else 0).coerceAtLeast(1),
            rhyme = sounded.substring(lastGroup),
            silent = if (silentE) 1 else 0,
            known = false,
            language = language,
        )
    }
}
