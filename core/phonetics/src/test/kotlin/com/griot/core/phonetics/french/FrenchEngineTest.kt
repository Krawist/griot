package com.griot.core.phonetics.french

import com.griot.core.phonetics.PhoneticEngines
import com.griot.core.phonetics.PhoneticLanguage
import com.griot.core.phonetics.RHYMES_SHOWN
import com.griot.core.phonetics.RhymingWord
import com.griot.core.phonetics.SyllableCount
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class FrenchEngineTest {
    private val engine = PhoneticEngines().french

    @Nested
    inner class CountSyllables {
        @ParameterizedTest
        @CsvSource(
            delimiter = '|',
            value = [
                // A final mute e is not counted.
                "la ville|2",
                // A mute e before a consonant counts.
                "la ville dort|4",
                // A mute e before a vowel is elided.
                "la ville a froid|4",
                // A plural or verbal mute e counts the same.
                "les villes dorment|4",
                "ils chantent encore|4",
                // Elisions follow the text.
                "l'amour d'un soir|4",
                "qu'il est beau|3",
            ],
        )
        fun `should follow the conventions of French songs`(verse: String, syllables: Int) {
            // when
            val count = engine.countSyllables(verse)

            // then
            count shouldBe SyllableCount(syllables = syllables)
        }

        @Test
        fun `should flag a count with a word missing from the dictionary`() {
            // when
            val count = engine.countSyllables("un soir à Bonabéri")

            // then
            count shouldBe SyllableCount(syllables = 7, guessedWords = listOf("Bonabéri"))
        }

        @Test
        fun `should count an English word with the English engine`() {
            // when
            // « danse » is elided before the vowel of « all ».
            val count = engine.countSyllables("je danse all night")

            // then
            count shouldBe SyllableCount(syllables = 4)
        }
    }

    @Nested
    inner class SetCorrections {
        @Test
        fun `should count a corrected word as the user said, no longer guessed`() {
            // given
            val engine = PhoneticEngines().french

            // when
            engine.setCorrections(mapOf("bonabéri" to 3))

            // then
            engine.countSyllables("un soir à Bonabéri") shouldBe SyllableCount(syllables = 6)
        }
    }

    @Nested
    inner class RhymeOf {
        @ParameterizedTest
        @CsvSource(
            "Je marche seul dans la ville, Mon cœur bat fragile",
            "Sur le bitume, Dans la brume",
            "Elle est clean, C'est sublime",
            "Il court plus vite, Le verre est vide",
            "Je suis riche, Droit comme une tige",
        )
        fun `should give verses that rhyme the same key`(first: String, second: String) {
            // when
            val keys = engine.rhyme(first)?.key to engine.rhyme(second)?.key

            // then
            keys.first shouldBe keys.second
        }

        @Test
        fun `should give verses that do not rhyme different keys`() {
            // when
            val keys = engine.rhyme("Je marche seul dans la ville")?.key to engine.rhyme("Sur le bitume")?.key

            // then
            keys.first shouldNotBe keys.second
        }

        @ParameterizedTest
        @CsvSource(
            "Je marche seul dans la ville, ille",
            "Et je chante, ante",
            "Toute la nuit., uit",
            "Une chanson, on",
            "La nuit est tranquille, ille",
        )
        fun `should highlight only the rhyming ending`(verse: String, ending: String) {
            // when
            val rhyme = engine.rhyme(verse)

            // then
            rhyme?.let { verse.substring(it.start, it.end) } shouldBe ending
        }

        @Test
        fun `should rhyme a word missing from the dictionary with the dictionary's words`() {
            // when
            val keys = engine.rhyme("Je rentre à Bonabéri")?.key to engine.rhyme("Le soleil de midi")?.key

            // then
            keys.first shouldBe keys.second
        }

        @Test
        fun `should give no rhyme to a verse without a word`() {
            // when
            val rhyme = engine.rhyme(" … ")

            // then
            rhyme.shouldBeNull()
        }
    }

    @Nested
    inner class Transcribe {
        @Test
        fun `should read a word of the dictionary`() {
            // when
            val transcription = engine.transcribe("ville")

            // then
            (transcription.phonemes.joinToString(separator = "") to transcription.known) shouldBe ("vil" to true)
            transcription.language shouldBe PhoneticLanguage.FRENCH
        }
    }

    @Nested
    inner class RhymingWords {
        @Test
        fun `should suggest the most common words with the rhyme of the verse first`() {
            // when
            val words = engine.rhymingWords("Les néons pleurent sur le bitume")

            // then
            words.take(3) shouldContainExactly listOf(
                RhymingWord(word = "fume", syllables = 1),
                RhymingWord(word = "brume", syllables = 1),
                RhymingWord(word = "plume", syllables = 1),
            )
        }

        @Test
        fun `should order the words by number of syllables`() {
            // when
            val words = engine.rhymingWords("Je marche seul dans la ville")

            // then
            words.map { it.syllables } shouldBe words.map { it.syllables }.sorted()
        }

        @Test
        fun `should suggest the rich rhymes first`() {
            // when
            val words = engine.rhymingWords("Je reviens encore")

            // then
            words.take(3) shouldContainExactly listOf(
                RhymingWord(word = "corps", syllables = 1),
                RhymingWord(word = "score", syllables = 1),
                RhymingWord(word = "accord", syllables = 2),
            )
        }

        @Test
        fun `should suggest at most ten words`() {
            // when
            val words = engine.rhymingWords("Je marche seul dans la ville")

            // then
            words shouldHaveSize RHYMES_SHOWN
        }

        @Test
        fun `should leave out the words that sound like the last word of the verse`() {
            // when
            val words = engine.rhymingWords("Le verre est vide")

            // then
            words.map { it.word } shouldNotContain "vides"
        }

        @Test
        fun `should leave out the last word of the verse`() {
            // when
            val words = engine.rhymingWords("Je marche seul dans la ville")

            // then
            words.map { it.word } shouldNotContain "ville"
        }

        @Test
        fun `should suggest English rhymes for a verse ending with an English word`() {
            // when
            val words = engine.rhymingWords("Je danse toute la night")

            // then
            words.first() shouldBe RhymingWord(word = "right", syllables = 1)
        }
    }
}
