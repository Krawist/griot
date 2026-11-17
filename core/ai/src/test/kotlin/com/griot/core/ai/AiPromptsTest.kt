package com.griot.core.ai

import com.griot.core.phonetics.PhoneticLanguage
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class AiPromptsTest {
    @Nested
    inner class RephrasePrompt {
        @Test
        fun `should give the section, the neighbouring verses and the target count`() {
            // given
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "my-section",
                verse = "my-verse",
                before = listOf("my-verse-before-1", "my-verse-before-2"),
                after = listOf("my-verse-after"),
                targetSyllables = 8,
            )

            // when
            val prompt = rephrasePrompt(request)

            // then
            prompt.request shouldBe """
                Section: my-section
                Verses before:
                my-verse-before-1
                my-verse-before-2
                Verse to rephrase: my-verse
                Verses after:
                my-verse-after
                Syllables of the verse: 8
            """.trimIndent()
        }

        @ParameterizedTest
        @CsvSource("FRENCH, French", "ENGLISH, English")
        fun `should ask an answer in the language of the song`(language: PhoneticLanguage, name: String) {
            // given
            val request = VerseRequest(
                language = language,
                section = "my-section",
                verse = "my-verse",
                before = emptyList(),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val prompt = rephrasePrompt(request)

            // then
            prompt.instructions shouldContain "Answer in $name."
        }

        @Test
        fun `should leave out the neighbours of a verse alone in its section`() {
            // given
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "my-section",
                verse = "my-verse",
                before = emptyList(),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val prompt = rephrasePrompt(request)

            // then
            prompt.request shouldNotContain "Verses"
        }
    }

    @Nested
    inner class CritiquePrompt {
        @Test
        fun `should give the counts of the section and forbid a rewritten verse`() {
            // given
            val request = VerseRequest(
                language = PhoneticLanguage.ENGLISH,
                section = "my-section",
                verse = "my-verse",
                before = listOf("my-verse-before"),
                after = emptyList(),
                targetSyllables = 7,
            )

            // when
            val prompt = critiquePrompt(request = request, counts = listOf(8, 7))

            // then
            prompt.request shouldContain "Syllables of the verses of the section, in order: 8, 7"
            prompt.instructions shouldContain "Never write a new version of the verse"
        }
    }

    @Nested
    inner class AnswerLines {
        @Test
        fun `should keep the lines without bullets, numbers and quotes`() {
            // given
            val answer = """
                1. « my-line-1 »
                - "my-line-2"

                • my-line-3
            """.trimIndent()

            // when
            val lines = answerLines(answer)

            // then
            lines shouldContainExactly listOf("my-line-1", "my-line-2", "my-line-3")
        }

        @Test
        fun `should keep three lines at most, each once`() {
            // given
            val answer = "my-line-1\nmy-line-1\nmy-line-2\nmy-line-3\nmy-line-4"

            // when
            val lines = answerLines(answer)

            // then
            lines shouldContainExactly listOf("my-line-1", "my-line-2", "my-line-3")
        }
    }
}
