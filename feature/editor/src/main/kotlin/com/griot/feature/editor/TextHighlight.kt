package com.griot.feature.editor

import androidx.compose.ui.graphics.Color

/**
 * A part of a text drawn as a rhyme: a light background and a coloured underline.
 *
 * @property range The letters highlighted, as indices in the text.
 * @property background Fill behind them.
 * @property underline Line under them.
 */
internal data class TextHighlight(val range: IntRange, val background: Color, val underline: Color)
