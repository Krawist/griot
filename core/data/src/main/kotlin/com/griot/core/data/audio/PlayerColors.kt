package com.griot.core.data.audio

/**
 * The colours of the player, from its cover (ARGB).
 *
 * @property background A dark colour of the cover, darkened until white text keeps 4.5:1 on it.
 * @property glow The cover's lighter colour, as a soft glow on the left.
 * @property secondGlow Its other light colour, as a soft glow on the right.
 */
data class PlayerColors(val background: Int, val glow: Int, val secondGlow: Int) {
    companion object {
        /** Griot's own: night violet, magenta and orange, for a cover generated without artwork. */
        val Griot =
            PlayerColors(background = 0xFF2B1638.toInt(), glow = 0xFFE0287D.toInt(), secondGlow = 0xFFFF7A1A.toInt())

        /** The least contrast of white text on [background]. */
        const val MIN_CONTRAST = 4.5

        /**
         * The colours from the swatches of a cover: [dark] as background (made dark enough for white text), [light]
         * and [secondLight] as glows; Griot's own where a swatch is missing.
         */
        fun from(dark: Int?, light: Int?, secondLight: Int?): PlayerColors = PlayerColors(
            background = readableBehindWhite(dark ?: Griot.background),
            glow = light ?: Griot.glow,
            secondGlow = secondLight ?: light ?: Griot.secondGlow,
        )

        /** [color], darkened step by step until white on it reaches [MIN_CONTRAST]. */
        fun readableBehindWhite(color: Int): Int {
            var current = color or OPAQUE
            var step = 0
            while (contrastWithWhite(current) < MIN_CONTRAST && step < MAX_STEPS) {
                current = darken(current)
                step++
            }
            return current
        }

        /** The contrast of white on [color], from 1 to 21. */
        fun contrastWithWhite(color: Int): Double = (1.0 + LUMINANCE_OFFSET) / (luminance(color) + LUMINANCE_OFFSET)

        private fun luminance(color: Int): Double {
            fun channel(shift: Int): Double {
                val value = (color shr shift and BYTE) / BYTE.toDouble()
                return if (value <=
                    LINEAR_LIMIT
                ) {
                    value / LINEAR_DIVISOR
                } else {
                    Math.pow((value + GAMMA_OFFSET) / GAMMA_DIVISOR, GAMMA)
                }
            }
            return RED_WEIGHT * channel(RED_SHIFT) + GREEN_WEIGHT * channel(GREEN_SHIFT) + BLUE_WEIGHT * channel(0)
        }

        private fun darken(color: Int): Int {
            fun channel(shift: Int) = ((color shr shift and BYTE) * DARKEN).toInt() shl shift
            return OPAQUE or channel(RED_SHIFT) or channel(GREEN_SHIFT) or channel(0)
        }

        private const val OPAQUE = 0xFF000000.toInt()
        private const val BYTE = 0xFF
        private const val RED_SHIFT = 16
        private const val GREEN_SHIFT = 8
        private const val DARKEN = 0.85
        private const val MAX_STEPS = 40
        private const val LUMINANCE_OFFSET = 0.05
        private const val LINEAR_LIMIT = 0.03928
        private const val LINEAR_DIVISOR = 12.92
        private const val GAMMA_OFFSET = 0.055
        private const val GAMMA_DIVISOR = 1.055
        private const val GAMMA = 2.4
        private const val RED_WEIGHT = 0.2126
        private const val GREEN_WEIGHT = 0.7152
        private const val BLUE_WEIGHT = 0.0722
    }
}
