package com.griot.core.design.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.TweenSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class GriotMotionTest {
    @Nested
    inner class Tween {
        @Test
        fun `should last the given duration`() {
            // given
            val motion = GriotMotion(reduced = false)

            // when
            val spec = motion.tween<Float>(duration = GriotMotion.LONG)

            // then
            spec.shouldBeInstanceOf<TweenSpec<Float>>().durationMillis shouldBe GriotMotion.LONG
        }

        @Test
        fun `should snap when the phone asks for no animations`() {
            // given
            val motion = GriotMotion(reduced = true)

            // when
            val spec = motion.tween<Float>(duration = GriotMotion.LONG)

            // then
            spec.shouldBeInstanceOf<SnapSpec<Float>>()
        }
    }

    @Nested
    inner class ColorChange {
        @Test
        fun `should snap when the phone asks for no animations`() {
            // given
            val motion = GriotMotion(reduced = true)

            // when
            val spec = motion.colorChange<Float>()

            // then
            spec.shouldBeInstanceOf<SnapSpec<Float>>()
        }
    }

    @Nested
    inner class SheetEnter {
        @Test
        fun `should rise from the bottom`() {
            // given
            val motion = GriotMotion(reduced = false)

            // when
            val transition = motion.sheetEnter()

            // then
            transition shouldNotBe EnterTransition.None
        }

        @Test
        fun `should not move when the phone asks for no animations`() {
            // given
            val motion = GriotMotion(reduced = true)

            // when
            val transition = motion.sheetEnter()

            // then
            transition shouldBe EnterTransition.None
        }
    }

    @Nested
    inner class SheetExit {
        @Test
        fun `should not move when the phone asks for no animations`() {
            // given
            val motion = GriotMotion(reduced = true)

            // when
            val transition = motion.sheetExit()

            // then
            transition shouldBe ExitTransition.None
        }
    }

    @Nested
    inner class ScreenEnter {
        @Test
        fun `should not move when the phone asks for no animations`() {
            // given
            val motion = GriotMotion(reduced = true)

            // when
            val transition = motion.screenEnter()

            // then
            transition shouldBe EnterTransition.None
        }
    }
}
