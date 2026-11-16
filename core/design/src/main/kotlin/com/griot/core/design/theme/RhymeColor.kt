package com.griot.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colour of one rhyme in the editor, for the current mode. Only the rhyming ending is highlighted, and the margin
 * letter (A, B, C…) always doubles the colour.
 *
 * @property underline Line drawn under the rhyming ending.
 * @property highlight Translucent fill behind the rhyming ending.
 * @property letter Margin letter of the rhyme; reaches 4.5:1 on the background and on [highlight].
 */
@Immutable
data class RhymeColor(val underline: Color, val highlight: Color, val letter: Color)

// Order of appearance: turquoise, orange, magenta, yellow, blue, green, violet, ochre.
internal val LightRhymeColors = listOf(
    RhymeColor(underline = Color(0xFF00B3A6), highlight = Color(0x3800B3A6), letter = Color(0xFF006D65)),
    RhymeColor(underline = Color(0xFFFF7A1A), highlight = Color(0x38FF7A1A), letter = Color(0xFFA04800)),
    RhymeColor(underline = Color(0xFFE0287D), highlight = Color(0x2EE0287D), letter = Color(0xFFB01A60)),
    RhymeColor(underline = Color(0xFFE0A800), highlight = Color(0x4DFFC93C), letter = Color(0xFF7A5500)),
    RhymeColor(underline = Color(0xFF3B7BF0), highlight = Color(0x2E3B7BF0), letter = Color(0xFF1A55B0)),
    RhymeColor(underline = Color(0xFF3BA55C), highlight = Color(0x2E3BA55C), letter = Color(0xFF246B2C)),
    RhymeColor(underline = Color(0xFF7B2FF7), highlight = Color(0x267B2FF7), letter = Color(0xFF6A22D8)),
    RhymeColor(underline = Color(0xFFB8761F), highlight = Color(0x2EB8761F), letter = Color(0xFF7E5210)),
)

internal val DarkRhymeColors = listOf(
    RhymeColor(underline = Color(0xFF3FD9CC), highlight = Color(0x5900B3A6), letter = Color(0xFF3FD9CC)),
    RhymeColor(underline = Color(0xFFFF9B52), highlight = Color(0x59FF7A1A), letter = Color(0xFFFF9B52)),
    RhymeColor(underline = Color(0xFFFF7AB6), highlight = Color(0x59E0287D), letter = Color(0xFFFF7AB6)),
    RhymeColor(underline = Color(0xFFFFC93C), highlight = Color(0x4DFFC93C), letter = Color(0xFFFFC93C)),
    RhymeColor(underline = Color(0xFF7FB0FF), highlight = Color(0x593B7BF0), letter = Color(0xFF7FB0FF)),
    RhymeColor(underline = Color(0xFF6FD98A), highlight = Color(0x4D3BA55C), letter = Color(0xFF6FD98A)),
    RhymeColor(underline = Color(0xFFB48CFF), highlight = Color(0x597B2FF7), letter = Color(0xFFB48CFF)),
    RhymeColor(underline = Color(0xFFD9A15A), highlight = Color(0x59B8761F), letter = Color(0xFFD9A15A)),
)
