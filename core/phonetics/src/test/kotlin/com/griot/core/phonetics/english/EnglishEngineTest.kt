package com.griot.core.phonetics.english

import com.griot.core.phonetics.PhoneticEngines
import com.griot.core.phonetics.SyllableCount
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class EnglishEngineTest {
    private val engine = PhoneticEngines().english

    @Nested
    inner class CountSyllables {
        @ParameterizedTest
        @CsvSource("I walk alone, 4", "every light, 4", "don't let go, 3", "a well-known song, 4")
        fun `should count the vowel phonemes`(verse: String, syllables: Int) {
            // when
            val count = engine.countSyllables(verse)

            // then
            count shouldBe SyllableCount(syllables = syllables, guessed = false)
        }

        @Test
        fun `should flag a count with a word missing from the dictionary`() {
            // when
            val count = engine.countSyllables("we dance in Bonaberi")

            // then
            count.guessed shouldBe true
        }
    }

    @Nested
    inner class RhymeOf {
        @ParameterizedTest
        @CsvSource(
            "Hold me tight, Before the light",
            "Into the blue, Waiting for you",
            "Out in the rain, Feel the pain",
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
            val keys = engine.rhyme("Hold me tight")?.key to engine.rhyme("Into the blue")?.key

            // then
            keys.first shouldNotBe keys.second
        }

        @ParameterizedTest
        @CsvSource("Hold me tight, ight", "It's time, ime", "Fading away, ay")
        fun `should highlight only the rhyming ending`(verse: String, ending: String) {
            // when
            val rhyme = engine.rhyme(verse)

            // then
            rhyme?.let { verse.substring(it.start, it.end) } shouldBe ending
        }
    }
}
