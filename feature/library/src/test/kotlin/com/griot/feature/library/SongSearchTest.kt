package com.griot.feature.library

import com.griot.core.data.model.LibrarySong
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class SongSearchTest {
    @Nested
    inner class Matches {
        @ParameterizedTest
        @ValueSource(strings = ["néons", "neons", "NEONS", " Néons "])
        fun `should find a title loosely`(query: String) {
            // given
            val song = mockk<LibrarySong> {
                every { title } returns "Les néons"
                every { lyrics } returns ""
            }

            // when
            val found = song.matches(query)

            // then
            found.shouldBeTrue()
        }

        @ParameterizedTest
        @ValueSource(strings = ["coeur", "c'est", "fragile"])
        fun `should find a verse loosely`(query: String) {
            // given
            val song = mockk<LibrarySong> {
                every { title } returns "my-title"
                every { lyrics } returns "Mon cœur bat, fragile\nC’est la nuit"
            }

            // when
            val found = song.matches(query)

            // then
            found.shouldBeTrue()
        }

        @Test
        fun `should not find a word the song does not hold`() {
            // given
            val song = mockk<LibrarySong> {
                every { title } returns "Les néons"
                every { lyrics } returns "Je marche seul dans la ville"
            }

            // when
            val found = song.matches("pluie")

            // then
            found.shouldBeFalse()
        }

        @Test
        fun `should match every song with a blank query`() {
            // given
            val song = mockk<LibrarySong>()

            // when
            val found = song.matches("  ")

            // then
            found.shouldBeTrue()
        }
    }
}
