package com.griot.core.phonetics.french

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
import com.griot.core.phonetics.wordsOf
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

// Vowels of Lexique's notation; j, w and 8 are semi-vowels, not syllables.
private const val VOWELS = "aieEoOuy2951@§°"

// Rhyme keys of this engine start with it.
private const val KEY_PREFIX = "fr:"

// The rhyming ending of a sound: from its last vowel to the end.
private fun ending(phonemes: String): String {
    val vowel = phonemes.indexOfLast { it in VOWELS }
    return if (vowel >= 0) phonemes.substring(vowel) else phonemes
}

// Spellings of a mute e at the end of a word, longest first.
private val MuteEndings = listOf("ent", "es", "e")

// Words written without their vowel before another word (« l'amour », « qu'il »): no syllable of their own.
private val Elided = setOf("l", "d", "j", "m", "t", "s", "n", "c", "qu", "jusqu", "lorsqu", "puisqu", "quoiqu")

/**
 * The French engine, on Lexique. Syllables follow French songwriting: a final mute e is not counted, a mute e before
 * a vowel is elided, a mute e before a consonant counts, and liaisons and elisions follow the written text.
 *
 * @param other The English engine, for English words of a French song.
 */
// One function per way of reading a word: dictionary, elision, compound, other language, guess.
@Suppress("TooManyFunctions")
class FrenchEngine internal constructor(
    private val dictionary: PhoneticDictionary,
    private val ranks: WordRanks,
    private val other: () -> PhoneticEngine?,
) : PhoneticEngine {
    /**
     * What a word brings to a verse.
     *
     * @property phonemes Its sound, in Lexique's notation (only its ending for a guess).
     * @property syllables Its syllables, a final mute e left out.
     * @property muteE Whether it ends with a mute e, counted before a consonant.
     * @property startsWithVowel Whether it starts with a vowel sound, which elides the mute e before it.
     * @property silent Letters at its end that are not heard, for the rhyme's highlight.
     * @property known Whether a dictionary has it.
     * @property language The engine that read it.
     */
    private data class Analysis(
        val phonemes: String,
        val syllables: Int,
        val muteE: Boolean,
        val startsWithVowel: Boolean,
        val silent: Int,
        val known: Boolean,
        val language: PhoneticLanguage,
    )

    private val cache = ConcurrentHashMap<String, Analysis>()

    @Volatile
    private var corrections: Map<String, Int> = emptyMap()

    override val language = PhoneticLanguage.FRENCH

    override fun lookup(word: String): Transcription? = dictionary[word.lowercase(Locale.FRENCH)]?.let { phonemes ->
        Transcription(phonemes = phonemes.map { it.toString() }, language = language, known = true)
    }

    override fun transcribe(word: String): Transcription {
        val analysis = analyse(word)
        return Transcription(
            phonemes = analysis.phonemes.map { it.toString() },
            language = analysis.language,
            known = analysis.known,
        )
    }

    override fun countSyllables(verse: String): SyllableCount {
        val words = wordsOf(verse).map { analyse(it.text) }
        var syllables = 0
        words.forEachIndexed { index, word ->
            syllables += word.syllables
            // A mute e inside the verse counts before a consonant, and is elided before a vowel.
            val next = words.getOrNull(index + 1)
            if (word.muteE && next != null && !next.startsWithVowel) syllables++
        }
        return SyllableCount(syllables = syllables, guessedWords = guessedWords(verse))
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
        val ending = ending(analysis.phonemes)
        // An English word ends the verse: its rhyme is the English engine's.
        return if (analysis.language == PhoneticLanguage.ENGLISH) {
            other()?.rhyme(verse)
        } else {
            Rhyme(
                key = "$KEY_PREFIX$ending",
                start = last.start + rhymeStart(last.text, analysis.silent),
                end = last.end,
            )
        }
    }

    override fun rhymingWords(verse: String): List<RhymingWord> {
        val key = rhyme(verse)?.key
        return when {
            key == null -> emptyList()
            // An English word ends the verse: its rhymes are the English engine's.
            !key.startsWith(KEY_PREFIX) -> other()?.rhymingWords(verse).orEmpty()
            else -> rhymesOf(ending = key.removePrefix(KEY_PREFIX), own = wordsOf(verse).last().text)
        }
    }

    // The words of the dictionary ending with the sound [ending], but the verse's own word.
    private fun rhymesOf(ending: String, own: String): List<RhymingWord> {
        val own = own.lowercase(Locale.FRENCH)
        val candidates = mutableListOf<RhymingWord>()
        dictionary.forEach { word, phonemes ->
            if (word != own && isSuggestible(word) && ending(phonemes) == ending) {
                candidates += RhymingWord(word = word, syllables = known(word, phonemes).syllables)
            }
        }
        return chooseRhymes(candidates = candidates, rank = { ranks[it] })
    }

    private fun analyse(word: String): Analysis = cache.getOrPut(word.lowercase(Locale.FRENCH)) {
        val lower = word.lowercase(Locale.FRENCH)
        val analysis = dictionary[lower]?.let { known(lower, it) }
            ?: elided(lower)
            ?: compound(lower)
            ?: foreign(lower)
            ?: guess(lower)
        // The user's count wins, and the word is no longer a guess.
        corrections[lower]?.let { syllables -> analysis.copy(syllables = syllables, known = true) } ?: analysis
    }

    private fun known(word: String, phonemes: String): Analysis {
        val endsWithVowelSound = phonemes.last() in VOWELS
        val muteE = !endsWithVowelSound && (word.endsWith("e") || word.endsWith("es") || word.endsWith("ent"))
        val silent = when {
            // The mute ending itself: « ent », « es » or « e ».
            muteE -> MuteEndings.first { word.endsWith(it) }.length
            // Letters after the last vowel sound that are not heard (« nuit », « chats »).
            endsWithVowelSound -> word.takeLastWhile { it in "stxdzp" }.length.coerceAtMost(word.length - 1)
            else -> 0
        }
        return Analysis(
            phonemes = phonemes,
            syllables = phonemes.count { it in VOWELS },
            muteE = muteE,
            startsWithVowel = phonemes.first() in VOWELS,
            silent = silent,
            known = true,
            language = language,
        )
    }

    // « l'amour », « qu'il »: the word after the apostrophe, which starts with a vowel.
    private fun elided(word: String): Analysis? {
        val apostrophe = word.lastIndexOf('\'')
        if (apostrophe < 0 || word.substring(0, apostrophe) !in Elided) return null
        return analyse(word.substring(apostrophe + 1)).copy(startsWithVowel = false)
    }

    // « peut-être », « vas-y »: its parts, with the mute e of a part counted before a consonant.
    private fun compound(word: String): Analysis? {
        if ('-' !in word) return null
        val parts = word.split('-').filter { it.isNotEmpty() }.map { analyse(it) }
        val inner = parts.zipWithNext().count { (part, next) -> part.muteE && !next.startsWithVowel }
        val last = parts.last()
        return last.copy(
            syllables = parts.sumOf { it.syllables } + inner,
            startsWithVowel = parts.first().startsWithVowel,
            known = parts.all { it.known },
            phonemes = parts.joinToString(separator = "") { it.phonemes },
        )
    }

    // An English word in a French song, counted by the English engine.
    private fun foreign(word: String): Analysis? {
        val english = other() ?: return null
        return english.lookup(word)?.let { transcription ->
            Analysis(
                phonemes = transcription.phonemes.joinToString(separator = " "),
                syllables = english.countSyllables(word).syllables,
                muteE = false,
                startsWithVowel = word.first().let { it in "aeiouy" },
                silent = if (word.endsWith("e") && word.length > 2) 1 else 0,
                known = true,
                language = PhoneticLanguage.ENGLISH,
            )
        }
    }

    private fun guess(word: String): Analysis = Analysis(
        phonemes = FrenchGuess.ending(word),
        syllables = FrenchGuess.syllables(word),
        muteE = FrenchGuess.endsWithMuteE(word),
        startsWithVowel = word.first().let { it == 'h' || it in "aeiouyàâéèêîïôûœ" },
        silent = FrenchGuess.silentEnding(word),
        known = false,
        language = language,
    )
}
