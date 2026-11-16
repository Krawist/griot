package com.griot.feature.editor

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt

/**
 * Resizes the lyrics with two fingers: from the [size] when the pinch starts, scaled by the pinch, rounded to the sp.
 * One finger is left to the list, which still scrolls.
 */
internal fun Modifier.pinchToResize(size: Int, onSizeChange: (Int) -> Unit): Modifier = composed {
    val currentSize by rememberUpdatedState(size)
    val currentOnSizeChange by rememberUpdatedState(onSizeChange)
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            var start: Int? = null
            var scale = 1f
            do {
                // Seen before the list, so that a pinch does not scroll it.
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.count { it.pressed } >= 2) {
                    val from = start ?: currentSize.also { start = it }
                    scale *= event.calculateZoom()
                    val next = (from * scale).roundToInt()
                    if (next != currentSize) currentOnSizeChange(next)
                    event.changes.forEach { it.consume() }
                }
            } while (event.changes.any { it.pressed })
        }
    }
}
