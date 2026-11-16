package com.griot.core.phonetics.french

import com.griot.core.phonetics.isVowelLetter
import com.griot.core.phonetics.vowelGroups

// Vowel spellings, longest first, and their sound in Lexique's notation.
private val Vowels = listOf(
    "eau" to "o", "œu" to "9", "au" to "o", "ou" to "u", "où" to "u", "oû" to "u", "oi" to "wa", "oî" to "wa",
    "ai" to "E", "aî" to "E", "ei" to "E", "eu" to "2", "ui" to "8i", "a" to "a", "à" to "a", "â" to "a",
    "é" to "e", "è" to "E", "ê" to "E", "ë" to "E", "e" to "E", "i" to "i", "î" to "i", "ï" to "i", "y" to "i",
    "o" to "o", "ô" to "o", "u" to "y", "û" to "y", "ü" to "y",
)

// Nasal vowels: a vowel spelling followed by n or m that no vowel follows.
private val Nasals = listOf(
    "ain" to "5", "aim" to "5", "ein" to "5", "oin" to "w5", "ien" to "j5", "an" to "@", "am" to "@", "en" to "@",
    "em" to "@", "in" to "5", "im" to "5", "yn" to "5", "on" to "§", "om" to "§", "un" to "1", "um" to "1",
)

// Consonant spellings, longest first, and their sound.
private val Consonants = listOf(
    "ill" to "j", "ch" to "S", "ph" to "f", "gn" to "N", "qu" to "k", "ll" to "l", "ss" to "s", "c" to "k", "ç" to "s",
    "g" to "g", "j" to "Z", "r" to "R", "x" to "ks", "h" to "", "w" to "w",
)

// Letters not heard at the end of a French word ending in a vowel sound (« nuit », « chats », « nez »).
private const val SILENT_FINALS = "stxdzp"

/**
 * A guess, from its spelling, of how a French word missing from Lexique sounds: its syllables, whether it ends with
 * a mute e, and its ending in Lexique's notation, enough to rhyme it with the words of the dictionary.
 */
internal object FrenchGuess {
    /** The number of letters at the end of [word] that are not heard: a mute e (« e », « es »), or « s », « t »… */
    fun silentEnding(word: String): Int {
        val muteE = muteELength(word)
        if (muteE > 0) return muteE
        val silent = word.takeLastWhile { it in SILENT_FINALS }.length
        return if (silent < word.length - 1) silent else 0
    }

    /** Whether [word] ends with a mute e, which counts before a consonant inside a verse. */
    fun endsWithMuteE(word: String): Boolean = muteELength(word) > 0

    // « porte » → 1, « portes » → 2; a word of one vowel group (« le », « les ») has no mute e.
    private fun muteELength(word: String): Int = when {
        vowelGroups(word).size < 2 -> 0
        word.endsWith("es") && !word.dropLast(2).last().isVowelLetter() -> 2
        word.endsWith("e") && !word.dropLast(1).last().isVowelLetter() -> 1
        else -> 0
    }

    /** The syllables of [word]: its groups of vowel letters, but a mute e. */
    fun syllables(word: String): Int = (vowelGroups(word).size - if (endsWithMuteE(word)) 1 else 0).coerceAtLeast(1)

    /** The sound of the end of [word], from its last vowel, in Lexique's notation (« cité » → « te »). */
    fun ending(word: String): String {
        val sounded = word.dropLast(silentEnding(word))
        val group = vowelGroups(sounded).lastOrNull() ?: return sounded
        val tail = sounded.substring(group.first)
        val after = sounded.substring(group.last + 1)
        val vowelSpelling = sounded.substring(group.first, group.last + 1)
        // A nasal vowel: its spelling followed by n or m that no vowel follows (« chanson », « parfum »).
        val nasal = Nasals.firstOrNull { (spelling, _) ->
            tail.startsWith(spelling) && (tail.length == spelling.length || !tail[spelling.length].isVowelLetter())
        }
        return when {
            nasal != null && after.length <= 2 -> nasal.second + consonants(tail.substring(nasal.first.length))
            vowelSpelling.endsWith("e") && after in setOf("r", "z") && group.last == sounded.length - 2 -> "e"
            vowelSpelling == "e" && after == "t" -> "E"
            else -> vowel(vowelSpelling) + consonants(after)
        }
    }

    private fun vowel(spelling: String): String {
        var rest = spelling
        val sound = StringBuilder()
        while (rest.isNotEmpty()) {
            val (written, heard) = Vowels.firstOrNull { rest.startsWith(it.first) } ?: (rest.take(1) to "")
            sound.append(heard)
            rest = rest.drop(written.length)
        }
        // The last vowel of the group is the one the rhyme starts at.
        return sound.toString()
    }

    private fun consonants(spelling: String): String {
        var rest = spelling
        val sound = StringBuilder()
        while (rest.isNotEmpty()) {
            val pair = Consonants.firstOrNull { rest.startsWith(it.first) }
            if (pair != null) {
                sound.append(pair.second)
                rest = rest.drop(pair.first.length)
            } else {
                // A doubled letter is heard once.
                if (sound.lastOrNull() != rest.first()) sound.append(rest.first())
                rest = rest.drop(1)
            }
        }
        return sound.toString()
    }
}
