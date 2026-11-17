package com.griot.core.phonetics

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class RhymeLettersTest {
    @Nested
    inner class RhymeLetters {
        @Test
        fun `should give the ranks in order of appearance, a verse without rhyme having none`() {
            // given
            val rhymes = listOf(
                Rhyme(key = "my-key-1", start = 0, end = 1),
                Rhyme(key = "my-key-2", start = 0, end = 1),
                null,
                Rhyme(key = "my-key-1", start = 0, end = 1),
            )

            // when
            val letters = rhymeLetters(rhymes)

            // then
            letters shouldContainExactly listOf(0, 1, null, 0)
        }
    }

    @Nested
    inner class RhymeLetter {
        @ParameterizedTest
        @CsvSource("0, A", "1, B", "25, Z", "26, A")
        fun `should name a rank by a letter`(rank: Int, letter: Char) {
            // when & then
            rhymeLetter(rank) shouldBe letter
        }
    }

    @Nested
    inner class RhymeScheme {
        @Test
        fun `should write one letter per verse with a rhyme`() {
            // when
            val scheme = rhymeScheme(listOf(0, 1, null, 0))

            // then
            scheme shouldBe "ABA"
        }
    }
}
