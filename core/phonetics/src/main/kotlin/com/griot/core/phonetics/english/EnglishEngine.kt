package com.griot.core.phonetics.english

import com.griot.core.phonetics.PhoneticDictionary
import com.griot.core.phonetics.PhoneticEngine
import com.griot.core.phonetics.PhoneticLanguage
import com.griot.core.phonetics.Rhyme
import com.griot.core.phonetics.RhymingWord
import com.griot.core.phonetics.SyllableCount
import com.griot.core.phonetics.Transcription
import com.griot.core.phonetics.WordRanks
import com.griot.core.phonetics.chooseRhymes
import com.griot.core.phonetics.isSuggestible
import com.griot.core.phonetics.rhymeStart
import com.griot.core.phonetics.vowelGroups
import com.griot.core.phonetics.wordsOf
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

// The rhyme key of a guessed word starts with it: its ending is letters, not sounds, so it only rhymes with guesses.
private const val GUESSED_KEY_PREFIX = "en~"

/**
 * The English engine, on the CMU Pronouncing Dictionary. Syllables are its vowel phonemes; a rhyme goes from the last
 * stressed vowel to the end of the word, its key written in Lexique's alphabet so that it can rhyme with French.
 *
 * @param other The French engine, for French words of an English song.
 */
// One function per way of reading a word: dictionary, elision, compound, other language, guess.
@Suppress("TooManyFunctions")
class EnglishEngine internal constructor(
    private val dictionary: PhoneticDictionary,
    private val ranks: WordRanks,
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

    @Volatile
    private var corrections: Map<String, Int> = emptyMap()

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
        return SyllableCount(syllables = words.sumOf { it.syllables }, guessedWords = guessedWords(verse))
    }

    override fun setCorrections(corrections: Map<String, Int>) {
        if (corrections == this.corrections) return
        this.corrections = corrections
        cache.clear()
    }

    private fun guessedWords(verse: String): List<String> = wordsOf(verse).map { it.text }.filter { !analyse(it).known }

    override fun rhyme(verse: String): Rhyme? {
        val last = wordsOf(verse).lastOrNull() ?: return null
        val analysis = analyse(last.text)
        // A French word ends the verse: its rhyme is the French engine's.
        return if (analysis.language == PhoneticLanguage.FRENCH) {
            other()?.rhyme(verse)
        } else {
            Rhyme(
                key = if (analysis.heard) inLexique(analysis.rhyme.split(' ')) else GUESSED_KEY_PREFIX + analysis.rhyme,
                start = last.start + rhymeStart(last.text, analysis.silent),
                end = last.end,
            )
        }
    }

    // The CMU dictionary holds many names: only the words of its frequency list are suggested.
    override fun rhymingWords(verse: String): List<RhymingWord> {
        val last = wordsOf(verse).lastOrNull() ?: return emptyList()
        val analysis = analyse(last.text)
        return when {
            // A French word ends the verse: its rhymes are the French engine's.
            analysis.language == PhoneticLanguage.FRENCH -> other()?.rhymingWords(verse).orEmpty()
            analysis.heard -> rhymesOf(ending = analysis.rhyme, own = last.text)
            else -> emptyList()
        }
    }

    // Whether the word's sounds are known, not guessed from its letters.
    private val Analysis.heard get() = known && phonemes.isNotEmpty()

    // The words of the frequency list ending with the sound [ending], but the verse's own word.
    private fun rhymesOf(ending: String, own: String): List<RhymingWord> {
        val own = own.lowercase(Locale.ENGLISH)
        val candidates = mutableListOf<RhymingWord>()
        dictionary.forEach { word, phonemes ->
            if (word != own && isSuggestible(word) && ranks[word] != Int.MAX_VALUE) {
                val analysis = known(word, phonemes.split(' '))
                if (analysis.rhyme == ending) candidates += RhymingWord(word = word, syllables = analysis.syllables)
            }
        }
        return chooseRhymes(candidates = candidates, rank = { ranks[it] })
    }

    private fun analyse(word: String): Analysis = cache.getOrPut(word.lowercase(Locale.ENGLISH)) {
        val lower = word.lowercase(Locale.ENGLISH)
        val analysis = dictionary[lower]?.let { known(lower, it.split(' ')) }
            ?: compound(lower)
            ?: foreign(lower)
            ?: guess(lower)
        // The user's count wins, and the word is no longer a guess.
        corrections[lower]?.let { syllables -> analysis.copy(syllables = syllables, known = true) } ?: analysis
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
