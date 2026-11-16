package com.griot.feature.editor

import com.griot.core.data.model.SongVerse
import com.griot.core.phonetics.PhoneticEngines
import com.griot.core.phonetics.SyllableCount
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// With the real French engine: the rhymes are those of Lexique.
internal class SongSoundsTest {
    private val engine = PhoneticEngines().french

    @Nested
    inner class SectionSoundsOf {
        @Test
        fun `should give the rhymes letters in order of appearance`() {
            // given
            val verses = listOf(
                SongVerse(id = "my-verse-id-1", text = "Je marche seul dans la ville"),
                SongVerse(id = "my-verse-id-2", text = "Les néons pleurent sur le bitume"),
                SongVerse(id = "my-verse-id-3", text = "Mon cœur bat, fragile"),
                SongVerse(id = "my-verse-id-4", text = "Comme une flamme dans la brume"),
            )

            // when
            val sounds = sectionSounds(verses = verses, engine = engine)

            // then
            sounds.scheme shouldBe "ABAB"
        }

        @Test
        fun `should give a verse its count, its letter and its highlighted ending`() {
            // given
            val verses = listOf(
                SongVerse(id = "my-verse-id-1", text = "Je marche seul dans la ville"),
                SongVerse(id = "my-verse-id-2", text = "Mon cœur bat, fragile"),
            )

            // when
            val sounds = sectionSounds(verses = verses, engine = engine)

            // then
            sounds.verses["my-verse-id-1"] shouldBe VerseSound(
                count = SyllableCount(syllables = 7),
                letter = 0,
                rhymes = true,
                ending = 24 until 28,
            )
        }

        @Test
        fun `should not colour a rhyme without a partner in its section`() {
            // given
            val verses = listOf(
                SongVerse(id = "my-verse-id-1", text = "Je marche seul dans la ville"),
                SongVerse(id = "my-verse-id-2", text = "Sur le bitume"),
            )

            // when
            val sounds = sectionSounds(verses = verses, engine = engine)

            // then
            sounds.verses["my-verse-id-2"]?.let { it.letter to it.rhymes } shouldBe (1 to false)
            sounds.scheme shouldBe "AB"
        }

        @Test
        fun `should leave an empty verse out of the scheme`() {
            // given
            val verses = listOf(
                SongVerse(id = "my-verse-id-1", text = "Je marche seul dans la ville"),
                SongVerse(id = "my-verse-id-2", text = ""),
            )

            // when
            val sounds = sectionSounds(verses = verses, engine = engine)

            // then
            sounds.verses["my-verse-id-2"] shouldBe VerseSound(
                count = SyllableCount(syllables = 0),
                letter = null,
                rhymes = false,
                ending = null,
            )
            sounds.scheme shouldBe "A"
        }
    }
}
