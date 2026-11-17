package com.griot.core.ai

import com.griot.core.phonetics.PhoneticEngines
import com.griot.core.phonetics.PhoneticLanguage
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// With the real French engine: counts and rhymes are those the editor shows.
internal class SuggestionCheckTest {
    private val engine = PhoneticEngines().of(PhoneticLanguage.FRENCH)

    @Nested
    inner class CheckSuggestions {
        @Test
        fun `should keep a suggestion with the same rhyme and count, counted by the engine`() {
            // given
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "Couplet 1",
                verse = "Le soleil cogne sur la tôle",
                before = emptyList(),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val checked = checkSuggestions(
                engine = engine,
                request = request,
                suggestions = listOf("Le soleil frappe sur la tôle"),
            )

            // then
            checked shouldContainExactly listOf(
                CheckedSuggestion(text = "Le soleil frappe sur la tôle", syllables = 8, guessed = false),
            )
        }

        @Test
        fun `should drop a suggestion that does not rhyme with the verse`() {
            // given
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "Couplet 1",
                verse = "Le soleil cogne sur la tôle",
                before = emptyList(),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val checked = checkSuggestions(
                engine = engine,
                request = request,
                suggestions = listOf("Le soleil frappe sur les toits"),
            )

            // then
            checked.shouldBeEmpty()
        }

        @Test
        fun `should drop a suggestion too far from the count of the verse`() {
            // given
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "Couplet 1",
                verse = "Le soleil cogne sur la tôle",
                before = emptyList(),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val checked = checkSuggestions(
                engine = engine,
                request = request,
                suggestions = listOf("Le grand soleil de midi cogne fort sur la tôle"),
            )

            // then
            checked.shouldBeEmpty()
        }

        @Test
        fun `should drop the verse itself given back`() {
            // given
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "Couplet 1",
                verse = "Le soleil cogne sur la tôle",
                before = emptyList(),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val checked = checkSuggestions(
                engine = engine,
                request = request,
                suggestions = listOf(" Le soleil cogne sur la tôle "),
            )

            // then
            checked.shouldBeEmpty()
        }
    }
}
