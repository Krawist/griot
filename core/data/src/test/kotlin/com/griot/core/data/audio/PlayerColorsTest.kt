package com.griot.core.data.audio

import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class PlayerColorsTest {
    @Nested
    inner class From {
        @ParameterizedTest
        @ValueSource(ints = [0xFFFFFFFF.toInt(), 0xFFFFC93C.toInt(), 0xFF3FD9CC.toInt(), 0xFF808080.toInt()])
        fun `should keep white readable on the background whatever the cover`(dark: Int) {
            // when
            val colors = PlayerColors.from(dark = dark, light = null, secondLight = null)

            // then
            PlayerColors.contrastWithWhite(colors.background) shouldBeGreaterThanOrEqual PlayerColors.MIN_CONTRAST
        }

        @Test
        fun `should keep a background already dark enough`() {
            // when
            val colors = PlayerColors.from(dark = 0xFF2B1638.toInt(), light = 0xFF111111.toInt(), secondLight = null)

            // then
            colors shouldBe PlayerColors(
                background = 0xFF2B1638.toInt(),
                glow = 0xFF111111.toInt(),
                secondGlow = 0xFF111111.toInt(),
            )
        }

        @Test
        fun `should take Griot's colours without swatches`() {
            // when
            val colors = PlayerColors.from(dark = null, light = null, secondLight = null)

            // then
            colors shouldBe PlayerColors.Griot
        }
    }
}
