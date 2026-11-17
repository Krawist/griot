package com.griot.feature.library

import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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

    @Nested
    inner class IsIn {
        @ParameterizedTest
        @CsvSource(
            "ALL, FRENCH, true",
            "FRENCH, FRENCH, true",
            "FRENCH, ENGLISH, false",
            "ENGLISH, ENGLISH, true",
            "ENGLISH, FRENCH, false",
        )
        fun `should filter on the language`(filter: LibraryFilter, language: SongLanguage, expected: Boolean) {
            // given
            val song = mockk<LibrarySong> { every { this@mockk.language } returns language }

            // when
            val shown = song.isIn(filter)

            // then
            shown shouldBe expected
        }

        @ParameterizedTest
        @CsvSource("DRAFT, true", "IN_PROGRESS, false")
        fun `should show only the drafts under Brouillons`(status: SongStatus, expected: Boolean) {
            // given
            val song = mockk<LibrarySong> { every { this@mockk.status } returns status }

            // when
            val shown = song.isIn(LibraryFilter.DRAFTS)

            // then
            shown shouldBe expected
        }
    }
}
