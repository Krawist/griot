package com.griot.core.data.export

import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.SongVerse
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class SongMarkdownTest {
    @Nested
    inner class SongMarkdownOf {
        @Test
        fun `should write the title, the author, then each section with its verses`() {
            // given
            val sections = listOf(
                SongSection(
                    id = "my-section-id-1",
                    kind = SectionKind.VERSE,
                    name = null,
                    repeatOf = null,
                    verses = listOf(
                        SongVerse(id = "my-verse-id-1", text = "a"),
                        SongVerse(id = "my-verse-id-2", text = "b "),
                    ),
                ),
                SongSection(
                    id = "my-section-id-2",
                    kind = SectionKind.CHORUS,
                    name = null,
                    repeatOf = "my-section-id-1",
                    verses = listOf(SongVerse(id = "my-verse-id-1", text = "a")),
                ),
            )

            // when
            val markdown = songMarkdown(
                title = "my-title",
                author = "my-author",
                sections = sections,
                labels = listOf("my-label-1", "my-label-2"),
            )

            // then
            markdown shouldBe "# my-title\n\n*my-author*\n\n## my-label-1\n\na  \nb\n\n## my-label-2\n\na\n"
        }

        @Test
        fun `should leave out an author not given`() {
            // when
            val markdown = songMarkdown(title = "my-title", author = " ", sections = emptyList(), labels = emptyList())

            // then
            markdown shouldBe "# my-title\n"
        }
    }

    @Nested
    inner class FileName {
        @ParameterizedTest
        @CsvSource(
            delimiter = '|',
            value = [
                "Les néons|Les néons.md",
                "AC/DC: live?|AC DC live.md",
                "  |Sans titre.md",
            ],
        )
        fun `should give a name every file system accepts`(title: String?, expected: String) {
            // when
            val name = fileName(title = title.orEmpty(), fallback = "Sans titre", extension = "md")

            // then
            name shouldBe expected
        }
    }

    @Nested
    inner class UniqueNames {
        @Test
        fun `should number the names met again`() {
            // when
            val names = uniqueNames(listOf("a.md", "b.md", "A.md", "a.md"))

            // then
            names shouldContainExactly listOf("a.md", "b.md", "A (2).md", "a (3).md")
        }
    }
}
