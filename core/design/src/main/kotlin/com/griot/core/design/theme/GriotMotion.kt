package com.griot.core.design.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * The motion language of Griot: the durations and easings every animation uses, and the transitions built from them.
 * With Android's « Supprimer les animations » ([reduced]), everything happens at once.
 *
 * @property reduced Whether the phone asks for no animations: every spec snaps and every transition is none.
 */
// One function per kind of motion of the app.
@Suppress("TooManyFunctions")
@Immutable
class GriotMotion(val reduced: Boolean) {
    /** A [duration] in milliseconds along [easing]; a snap when motion is reduced. */
    fun <T> tween(duration: Int = MEDIUM, easing: Easing = Standard): FiniteAnimationSpec<T> =
        if (reduced) snap() else tween(durationMillis = duration, easing = easing)

    /** A screen opened from another: it slides in a little from the side while it fades in. */
    fun screenEnter(): EnterTransition = enter {
        slideInHorizontally(tween<IntOffset>(LONG, Emphasized)) { it / SLIDE_FRACTION } + fadeIn(tween(MEDIUM))
    }

    /** The screen left for another: it fades out, quickly. */
    fun screenExit(): ExitTransition = exit { fadeOut(tween(SHORT, Exit)) }

    /** The screen coming back after a back: it slides in a little from the other side. */
    fun screenPopEnter(): EnterTransition = enter {
        slideInHorizontally(tween<IntOffset>(LONG, Emphasized)) { -it / SLIDE_FRACTION } + fadeIn(tween(MEDIUM))
    }

    /** The screen closed by a back: it slides away to the side while it fades out. */
    fun screenPopExit(): ExitTransition = exit {
        slideOutHorizontally(tween<IntOffset>(MEDIUM, Exit)) { it / SLIDE_FRACTION } + fadeOut(tween(SHORT, Exit))
    }

    /** A tab: it only fades, side by side with the others. */
    fun tabEnter(): EnterTransition = enter { fadeIn(tween(MEDIUM)) }

    /** The tab left for another. */
    fun tabExit(): ExitTransition = exit { fadeOut(tween(SHORT, Exit)) }

    /** A sheet that rises from the bottom, such as the AI panel. */
    fun sheetEnter(): EnterTransition = enter {
        slideInVertically(tween<IntOffset>(LONG, Emphasized)) { it } + fadeIn(tween(SHORT))
    }

    /** A sheet that goes back down. */
    fun sheetExit(): ExitTransition = exit {
        slideOutVertically(tween<IntOffset>(MEDIUM, Exit)) { it } + fadeOut(tween(MEDIUM, Exit))
    }

    /** A colour that changes softly, such as a rhyme colour appearing as the user types. */
    fun <T> colorChange(): FiniteAnimationSpec<T> = tween(LONG, Standard)

    /** A bar that rises from the bottom: the bottom bar, the editor toolbar. */
    fun barEnter(): EnterTransition = enter {
        slideInVertically(tween<IntOffset>(MEDIUM, Emphasized)) { it } + fadeIn(tween(SHORT))
    }

    /** A bar that goes back down. */
    fun barExit(): ExitTransition = exit {
        slideOutVertically(tween<IntOffset>(SHORT, Exit)) { it } + fadeOut(tween(SHORT, Exit))
    }

    /** A card that appears in a list: it opens its place and grows in. */
    fun itemEnter(): EnterTransition = enter {
        expandVertically(tween<IntSize>(MEDIUM, Emphasized), expandFrom = Alignment.Top) +
            scaleIn(tween(MEDIUM, Emphasized), initialScale = ITEM_SCALE) +
            fadeIn(tween(MEDIUM))
    }

    /** A card that leaves a list: it fades and its place closes, its top staying where it was. */
    fun itemExit(): ExitTransition = exit {
        fadeOut(tween(SHORT, Exit)) + shrinkVertically(tween<IntSize>(MEDIUM, Standard), shrinkTowards = Alignment.Top)
    }

    private inline fun enter(transition: () -> EnterTransition) = if (reduced) EnterTransition.None else transition()

    private inline fun exit(transition: () -> ExitTransition) = if (reduced) ExitTransition.None else transition()

    companion object {
        /** A touch answered, a fade out: 150 ms. */
        const val SHORT = 150

        /** Most changes of state: 250 ms. */
        const val MEDIUM = 250

        /** A screen coming in: 400 ms. */
        const val LONG = 400

        /** Changes of state within a screen: quick to start, gentle to end. */
        val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

        /** What comes in: it arrives fast and settles slowly. */
        val Emphasized: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

        /** What leaves: it starts slowly and goes away fast. */
        val Exit: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

        // Screens slide by a tenth of their width: enough to feel the direction, never a full swipe.
        private const val SLIDE_FRACTION = 10

        // A card grows from 92 % of its size.
        private const val ITEM_SCALE = 0.92f
    }
}
