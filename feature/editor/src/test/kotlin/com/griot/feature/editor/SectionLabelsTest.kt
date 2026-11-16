package com.griot.feature.editor

import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongSection
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SectionLabelsTest {
    @Nested
    inner class SectionLabels {
        @Test
        fun `should number the verses, alone or not`() {
            // given
            val sections = listOf(
                SongSection(
                    id = "my-id-1",
                    kind = SectionKind.VERSE,
                    name = null,
                    repeatOf = null,
                    verses = emptyList(),
                ),
            )

            // when
            val labels = sectionLabels(sections)

            // then
            labels shouldContainExactly listOf(SectionLabel(kind = SectionKind.VERSE, number = 1, name = null))
        }

        @Test
        fun `should number another kind only when there are several`() {
            // given
            val sections = listOf(
                SongSection(
                    id = "my-id-1",
                    kind = SectionKind.CHORUS,
                    name = null,
                    repeatOf = null,
                    verses = emptyList(),
                ),
                SongSection(
                    id = "my-id-2",
                    kind = SectionKind.BRIDGE,
                    name = null,
                    repeatOf = null,
                    verses = emptyList(),
                ),
                SongSection(
                    id = "my-id-3",
                    kind = SectionKind.BRIDGE,
                    name = null,
                    repeatOf = null,
                    verses = emptyList(),
                ),
            )

            // when
            val labels = sectionLabels(sections)

            // then
            labels shouldContainExactly listOf(
                SectionLabel(kind = SectionKind.CHORUS, number = null, name = null),
                SectionLabel(kind = SectionKind.BRIDGE, number = 1, name = null),
                SectionLabel(kind = SectionKind.BRIDGE, number = 2, name = null),
            )
        }

        @Test
        fun `should give a repeat the label of its source without counting it`() {
            // given
            val sections = listOf(
                SongSection(
                    id = "my-id-1",
                    kind = SectionKind.CHORUS,
                    name = null,
                    repeatOf = null,
                    verses = emptyList(),
                ),
                SongSection(
                    id = "my-id-2",
                    kind = SectionKind.VERSE,
                    name = null,
                    repeatOf = null,
                    verses = emptyList(),
                ),
                SongSection(
                    id = "my-id-3",
                    kind = SectionKind.CHORUS,
                    name = null,
                    repeatOf = "my-id-1",
                    verses = emptyList(),
                ),
            )

            // when
            val labels = sectionLabels(sections)

            // then
            labels shouldContainExactly listOf(
                SectionLabel(kind = SectionKind.CHORUS, number = null, name = null),
                SectionLabel(kind = SectionKind.VERSE, number = 1, name = null),
                SectionLabel(kind = SectionKind.CHORUS, number = null, name = null),
            )
        }

        @Test
        fun `should keep the rank of a named section for the next ones`() {
            // given
            val sections = listOf(
                SongSection(
                    id = "my-id-1",
                    kind = SectionKind.VERSE,
                    name = "my-name",
                    repeatOf = null,
                    verses = emptyList(),
                ),
                SongSection(
                    id = "my-id-2",
                    kind = SectionKind.VERSE,
                    name = null,
                    repeatOf = null,
                    verses = emptyList(),
                ),
            )

            // when
            val labels = sectionLabels(sections)

            // then
            labels shouldContainExactly listOf(
                SectionLabel(kind = SectionKind.VERSE, number = 1, name = "my-name"),
                SectionLabel(kind = SectionKind.VERSE, number = 2, name = null),
            )
        }
    }
}
