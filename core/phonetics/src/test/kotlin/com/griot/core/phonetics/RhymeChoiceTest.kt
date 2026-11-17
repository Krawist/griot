package com.griot.core.phonetics

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class RhymeChoiceTest {
    @Nested
    inner class ChooseRhymes {
        @Test
        fun `should put the best ranked words first, then the shortest`() {
            // given
            val candidates = listOf(
                RhymingWord(word = "my-word-long", syllables = 1),
                RhymingWord(word = "my-word", syllables = 1),
                RhymingWord(word = "my-word-ranked", syllables = 1),
            )

            // when
            val rhymes = chooseRhymes(candidates = candidates, rank = { if (it == "my-word-ranked") 0 else 1 })

            // then
            rhymes.map { it.word } shouldContainExactly listOf("my-word-ranked", "my-word", "my-word-long")
        }

        @Test
        fun `should group the words by number of syllables`() {
            // given
            val candidates = listOf(
                RhymingWord(word = "my-word-1", syllables = 2),
                RhymingWord(word = "my-word-2", syllables = 1),
            )

            // when
            val rhymes = chooseRhymes(candidates = candidates, rank = { 0 })

            // then
            rhymes.map { it.word } shouldContainExactly listOf("my-word-2", "my-word-1")
        }

        @Test
        fun `should leave out the words of too many syllables`() {
            // given
            val candidates = listOf(RhymingWord(word = "my-word", syllables = MAX_RHYME_SYLLABLES + 1))

            // when
            val rhymes = chooseRhymes(candidates = candidates, rank = { 0 })

            // then
            rhymes shouldHaveSize 0
        }

        @Test
        fun `should keep a few words per number of syllables`() {
            // given
            val candidates = (1..RHYMES_PER_COUNT + 5).map { RhymingWord(word = "my-word-$it", syllables = 1) }

            // when
            val rhymes = chooseRhymes(candidates = candidates, rank = { 0 })

            // then
            rhymes shouldHaveSize RHYMES_PER_COUNT
        }
    }
}
