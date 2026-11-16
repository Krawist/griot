package com.griot.core.design.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class GriotPaletteTest {
    @Nested
    inner class Contrast {
        @ParameterizedTest(name = "{2} {0}")
        @MethodSource("com.griot.core.design.theme.GriotPaletteTest#textColors")
        fun `should reach 4·5 to 1 for text on the background`(token: String, palette: GriotPalette) {
            // given
            val text = palette.textColor(token)

            // when
            val ratio = contrastRatio(text, palette.background)

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }

        @ParameterizedTest(name = "{2} {0}")
        @MethodSource("com.griot.core.design.theme.GriotPaletteTest#textColors")
        fun `should reach 4·5 to 1 for text on a glass surface`(token: String, palette: GriotPalette) {
            // given
            val text = palette.textColor(token)
            val glass = palette.glass.compositeOver(palette.background)

            // when
            val ratio = contrastRatio(text, glass)

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }

        @ParameterizedTest(name = "{2} {0}")
        @MethodSource("com.griot.core.design.theme.GriotPaletteTest#softFills")
        fun `should reach 4·5 to 1 for text on its soft fill`(token: String, palette: GriotPalette) {
            // given
            val (text, soft) = palette.softFill(token)

            // when
            val ratio = contrastRatio(text, soft.compositeOver(palette.background))

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource("com.griot.core.design.theme.GriotPaletteTest#palettes")
        fun `should reach 4·5 to 1 for text on a filled magenta button`(palette: GriotPalette) {
            // when
            val ratio = contrastRatio(palette.onAccent, palette.accentFill)

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource("com.griot.core.design.theme.GriotPaletteTest#palettes")
        fun `should reach 4·5 to 1 for text on an AI button`(palette: GriotPalette) {
            // when
            val ratio = contrastRatio(palette.onAi, palette.ai)

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource("com.griot.core.design.theme.GriotPaletteTest#palettes")
        fun `should reach 4·5 to 1 for text on an inverted control`(palette: GriotPalette) {
            // when
            val ratio = contrastRatio(palette.onInverse, palette.inverse)

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }

        @ParameterizedTest(name = "{3} rhyme {0}")
        @MethodSource("com.griot.core.design.theme.GriotPaletteTest#rhymeColors")
        fun `should reach 4·5 to 1 for the margin letter of a rhyme`(
            index: Int,
            palette: GriotPalette,
            rhymeColors: List<RhymeColor>,
        ) {
            // given
            val letter = rhymeColors[index].letter

            // when
            val ratio = contrastRatio(letter, palette.background)

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }

        @ParameterizedTest(name = "{3} rhyme {0}")
        @MethodSource("com.griot.core.design.theme.GriotPaletteTest#rhymeColors")
        fun `should reach 4·5 to 1 for the margin letter of a rhyme on its highlight`(
            index: Int,
            palette: GriotPalette,
            rhymeColors: List<RhymeColor>,
        ) {
            // given
            val rhyme = rhymeColors[index]

            // when
            val ratio = contrastRatio(rhyme.letter, rhyme.highlight.compositeOver(palette.background))

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }

        @ParameterizedTest(name = "{3} rhyme {0}")
        @MethodSource("com.griot.core.design.theme.GriotPaletteTest#rhymeColors")
        fun `should reach 4·5 to 1 for lyrics on a rhyme highlight`(
            index: Int,
            palette: GriotPalette,
            rhymeColors: List<RhymeColor>,
        ) {
            // given
            val highlight = rhymeColors[index].highlight.compositeOver(palette.background)

            // when
            val ratio = contrastRatio(palette.ink, highlight)

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }
    }

    // WCAG 2 contrast ratio between two opaque colours.
    private fun contrastRatio(first: Color, second: Color): Double {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun GriotPalette.textColor(token: String): Color = when (token) {
        "ink" -> ink
        "ink2" -> ink2
        "muted" -> muted
        "accentText" -> accentText
        "aiText" -> aiText
        "syncText" -> syncText
        "infoText" -> infoText
        else -> error("Unknown text token $token")
    }

    private fun GriotPalette.softFill(token: String): Pair<Color, Color> = when (token) {
        "accent" -> accentText to accentSoft
        "ai" -> aiText to aiSoft
        "sync" -> syncText to syncSoft
        "info" -> infoText to infoSoft
        "selection" -> ink to selection
        else -> error("Unknown soft fill $token")
    }

    companion object {
        private const val MIN_TEXT_CONTRAST = 4.5

        private val textTokens = listOf("ink", "ink2", "muted", "accentText", "aiText", "syncText", "infoText")

        private val softFillTokens = listOf("accent", "ai", "sync", "info", "selection")

        // The mode is the last argument: JUnit only uses it in the test name.
        @JvmStatic
        fun palettes(): List<Arguments> = listOf(
            Arguments.of(LightPalette, "light"),
            Arguments.of(DarkPalette, "dark"),
        )

        @JvmStatic
        fun textColors(): List<Arguments> = palettes().flatMap { arguments ->
            val (palette, mode) = arguments.get()
            textTokens.map { token -> Arguments.of(token, palette, mode) }
        }

        @JvmStatic
        fun softFills(): List<Arguments> = palettes().flatMap { arguments ->
            val (palette, mode) = arguments.get()
            softFillTokens.map { token -> Arguments.of(token, palette, mode) }
        }

        @JvmStatic
        fun rhymeColors(): List<Arguments> = listOf(
            Triple(LightPalette, LightRhymeColors, "light"),
            Triple(DarkPalette, DarkRhymeColors, "dark"),
        ).flatMap { (palette, rhymeColors, mode) ->
            rhymeColors.indices.map { index -> Arguments.of(index, palette, rhymeColors, mode) }
        }
    }
}
