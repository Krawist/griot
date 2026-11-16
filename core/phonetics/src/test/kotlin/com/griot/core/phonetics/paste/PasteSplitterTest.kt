package com.griot.core.phonetics.paste

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class PasteSplitterTest {
    @Nested
    inner class SplitPastedText {
        @Test
        fun `should keep a text without blank line nor label as one block`() {
            // when
            val blocks = splitPastedText("Je marche seul dans la ville\nLes néons pleurent sur le bitume")

            // then
            blocks shouldContainExactly listOf(
                PastedBlock(
                    kind = null,
                    label = null,
                    lines = listOf("Je marche seul dans la ville", "Les néons pleurent sur le bitume"),
                    repeatOf = null,
                ),
            )
        }

        @Test
        fun `should start a block at each blank line, however many`() {
            // when
            val blocks = splitPastedText("a\nb\n\n\n  \nc\r\nd\r\n")

            // then
            blocks shouldContainExactly listOf(
                PastedBlock(kind = null, label = null, lines = listOf("a", "b"), repeatOf = null),
                PastedBlock(kind = null, label = null, lines = listOf("c", "d"), repeatOf = null),
            )
        }

        @ParameterizedTest
        @CsvSource(
            delimiter = '|',
            value = [
                "[Refrain]|CHORUS|Refrain",
                "(Chorus)|CHORUS|Chorus",
                "Couplet 2 :|VERSE|Couplet",
                "COUPLET|VERSE|COUPLET",
                "[Verse 1]|VERSE|Verse",
                "Pont|BRIDGE|Pont",
                "[Bridge]|BRIDGE|Bridge",
                "Intro|INTRO|Intro",
                "(Outro)|OUTRO|Outro",
                "[Pre-chorus]|PRE_CHORUS|Pre-chorus",
                "Pré-refrain :|PRE_CHORUS|Pré-refrain",
                "[Chorus x2]|CHORUS|Chorus",
            ],
        )
        fun `should name the block after a label line, not kept as a verse`(
            line: String,
            kind: BlockKind,
            label: String,
        ) {
            // when
            val blocks = splitPastedText("$line\nmy-line")

            // then
            blocks shouldContainExactly listOf(
                PastedBlock(kind = kind, label = label, lines = listOf("my-line"), repeatOf = null),
            )
        }

        @ParameterizedTest
        @ValueSource(strings = ["Refrain final", "Le pont des soupirs", "Intro au piano doux"])
        fun `should keep a line that only starts like a label as a verse`(line: String) {
            // when
            val blocks = splitPastedText(line)

            // then
            blocks shouldContainExactly
                listOf(PastedBlock(kind = null, label = null, lines = listOf(line), repeatOf = null))
        }

        @Test
        fun `should name the block below a label even after a blank line`() {
            // when
            val blocks = splitPastedText("[Refrain]\n\nEt je chante")

            // then
            blocks shouldContainExactly listOf(
                PastedBlock(
                    kind = BlockKind.CHORUS,
                    label = "Refrain",
                    lines = listOf("Et je chante"),
                    repeatOf = null,
                ),
            )
        }

        @Test
        fun `should start a block at a label without a blank line before it`() {
            // when
            val blocks = splitPastedText("a\nb\n[Refrain]\nc")

            // then
            blocks shouldContainExactly listOf(
                PastedBlock(kind = null, label = null, lines = listOf("a", "b"), repeatOf = null),
                PastedBlock(kind = BlockKind.CHORUS, label = "Refrain", lines = listOf("c"), repeatOf = null),
            )
        }

        @Test
        fun `should repeat a block sung again, and make a plain one a chorus`() {
            // when
            val blocks = splitPastedText("a\nb\n\nEt je chante\nPour la nuit\n\nc\n\nEt je chante\npour la nuit")

            // then
            blocks shouldContainExactly listOf(
                PastedBlock(kind = null, label = null, lines = listOf("a", "b"), repeatOf = null),
                PastedBlock(
                    kind = BlockKind.CHORUS,
                    label = null,
                    lines = listOf("Et je chante", "Pour la nuit"),
                    repeatOf = null,
                ),
                PastedBlock(kind = null, label = null, lines = listOf("c"), repeatOf = null),
                PastedBlock(
                    kind = BlockKind.CHORUS,
                    label = null,
                    lines = listOf("Et je chante", "pour la nuit"),
                    repeatOf = 1,
                ),
            )
        }

        @Test
        fun `should repeat the last section of its kind for a label alone`() {
            // when
            val blocks = splitPastedText("[Refrain]\nEt je chante\n\n[Couplet 2]\nc\n\n[Refrain]\n\n[Outro]\nfin")

            // then
            blocks shouldContainExactly listOf(
                PastedBlock(
                    kind = BlockKind.CHORUS,
                    label = "Refrain",
                    lines = listOf("Et je chante"),
                    repeatOf = null,
                ),
                PastedBlock(kind = BlockKind.VERSE, label = "Couplet", lines = listOf("c"), repeatOf = null),
                PastedBlock(kind = BlockKind.CHORUS, label = "Refrain", lines = emptyList(), repeatOf = 0),
                PastedBlock(kind = BlockKind.OUTRO, label = "Outro", lines = listOf("fin"), repeatOf = null),
            )
        }

        @Test
        fun `should drop a label alone with nothing to repeat`() {
            // when
            val blocks = splitPastedText("[Refrain]\n\n")

            // then
            blocks.shouldBeEmpty()
        }

        // A note exported from Samsung Notes as .txt: the title on the first line, then the lyrics as typed.
        @Test
        fun `should split a note exported from Samsung Notes`() {
            // given
            val note = """
                Saison sèche

                Couplet 1
                La poussière danse sur nos rêves
                Le soleil cogne sur la tôle

                Refrain
                On attend la pluie
                Comme on attend la vie

                Couplet 2
                Les enfants courent après le vent

                Refrain
            """.trimIndent()

            // when
            val blocks = splitPastedText(note)

            // then
            blocks shouldContainExactly listOf(
                PastedBlock(kind = null, label = null, lines = listOf("Saison sèche"), repeatOf = null),
                PastedBlock(
                    kind = BlockKind.VERSE,
                    label = "Couplet",
                    lines = listOf("La poussière danse sur nos rêves", "Le soleil cogne sur la tôle"),
                    repeatOf = null,
                ),
                PastedBlock(
                    kind = BlockKind.CHORUS,
                    label = "Refrain",
                    lines = listOf("On attend la pluie", "Comme on attend la vie"),
                    repeatOf = null,
                ),
                PastedBlock(
                    kind = BlockKind.VERSE,
                    label = "Couplet",
                    lines = listOf("Les enfants courent après le vent"),
                    repeatOf = null,
                ),
                PastedBlock(kind = BlockKind.CHORUS, label = "Refrain", lines = emptyList(), repeatOf = 2),
            )
        }

        @Test
        fun `should give nothing for a blank text`() {
            // when
            val blocks = splitPastedText(" \n\n")

            // then
            blocks.shouldBeEmpty()
        }
    }
}
