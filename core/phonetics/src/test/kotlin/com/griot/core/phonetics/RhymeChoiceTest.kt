package com.griot.core.phonetics

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class RhymeChoiceTest {
    @Nested
    inner class ChooseRhymes {
        @Test
        fun `should put the common words before the rare ones, however rich`() {
            // given
            val candidates = listOf(
                RhymingWord(word = "my-word-1", syllables = 1),
                RhymingWord(word = "my-word-2", syllables = 1),
            )

            // when
            val rhymes = chooseRhymes(
                candidates = candidates,
                sound = { it },
                strength = { if (it == "my-word-1") 1 else 0 },
                rank = { if (it == "my-word-1") Int.MAX_VALUE else 5 },
            )

            // then
            rhymes.map { it.word } shouldContainExactly listOf("my-word-2", "my-word-1")
        }

        @Test
        fun `should put the richest rhymes first`() {
            // given
            val candidates = listOf(
                RhymingWord(word = "my-word-1", syllables = 1),
                RhymingWord(word = "my-word-2", syllables = 1),
            )

            // when
            val rhymes = chooseRhymes(
                candidates = candidates,
                sound = { it },
                strength = { if (it == "my-word-2") 1 else 0 },
                rank = { if (it == "my-word-1") 0 else 1 },
            )

            // then
            rhymes.map { it.word } shouldContainExactly listOf("my-word-2", "my-word-1")
        }

        @Test
        fun `should put the best ranked words first among rhymes as rich, then the shortest`() {
            // given
            val candidates = listOf(
                RhymingWord(word = "my-word-long", syllables = 1),
                RhymingWord(word = "my-word", syllables = 1),
                RhymingWord(word = "my-word-ranked", syllables = 1),
            )

            // when
            val rhymes = chooseRhymes(
                candidates = candidates,
                sound = { it },
                strength = { 2 },
                rank = { if (it == "my-word-ranked") 0 else 1 },
            )

            // then
            rhymes.map { it.word } shouldContainExactly listOf("my-word-ranked", "my-word", "my-word-long")
        }

        @Test
        fun `should keep the best ranked word of each pronunciation`() {
            // given
            val candidates = listOf(
                RhymingWord(word = "my-word-1", syllables = 1),
                RhymingWord(word = "my-word-2", syllables = 1),
            )

            // when
            val rhymes = chooseRhymes(
                candidates = candidates,
                sound = { "my-sound" },
                strength = { 0 },
                rank = { if (it == "my-word-2") 0 else 1 },
            )

            // then
            rhymes.map { it.word } shouldContainExactly listOf("my-word-2")
        }

        @Test
        fun `should give the words by number of syllables`() {
            // given
            val candidates = listOf(
                RhymingWord(word = "my-word-1", syllables = 2),
                RhymingWord(word = "my-word-2", syllables = 1),
            )

            // when
            val rhymes = chooseRhymes(candidates = candidates, sound = { it }, strength = { 2 }, rank = { 0 })

            // then
            rhymes.map { it.word } shouldContainExactly listOf("my-word-2", "my-word-1")
        }

        @Test
        fun `should leave out the words of too many syllables`() {
            // given
            val candidates = listOf(RhymingWord(word = "my-word", syllables = MAX_RHYME_SYLLABLES + 1))

            // when
            val rhymes = chooseRhymes(candidates = candidates, sound = { it }, strength = { 2 }, rank = { 0 })

            // then
            rhymes shouldHaveSize 0
        }

        @Test
        fun `should keep only the strongest rhymes`() {
            // given
            val candidates = (1..RHYMES_SHOWN + 5).map { RhymingWord(word = "my-word-$it", syllables = 1 + it % 2) }

            // when
            val rhymes = chooseRhymes(candidates = candidates, sound = { it }, strength = { 2 }, rank = { 0 })

            // then
            rhymes shouldHaveSize RHYMES_SHOWN
        }
    }

    @Nested
    inner class Richness {
        @Test
        fun `should find a rich rhyme when a sound before the ending is shared`() {
            // when
            val richness = richness(sounds = "@kOR".toList(), candidate = "dekOR".toList(), endingLength = 2)

            // then
            richness shouldBe 1
        }

        @Test
        fun `should find a plain rhyme when the word holds the whole verse's word`() {
            // when
            val richness = richness(sounds = "nAjt".toList(), candidate = "tunAjt".toList(), endingLength = 3)

            // then
            richness shouldBe 0
        }

        @Test
        fun `should find a plain rhyme when only the ending is shared`() {
            // when
            val richness = richness(sounds = "@kOR".toList(), candidate = "alOR".toList(), endingLength = 2)

            // then
            richness shouldBe 0
        }
    }
}
