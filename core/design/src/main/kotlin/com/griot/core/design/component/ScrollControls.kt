package com.griot.core.design.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

// How far a finger scrolls one way before the controls hide or come back.
private val ToggleDistance = 24.dp

/**
 * Whether a screen's controls (bars, header, floating buttons) show, from how the user scrolls its [list]: down hides
 * them, up brings them back, and they show at the top. Scrolls the app makes itself (to a focused field, to the top)
 * change nothing. Shared by the song list and the editor.
 *
 * Hiding a header gives its room to the list: a list only a little longer than the screen may then fit, and fall back
 * to its top. So the controls hide only while the list has more to show, and the top brings them back only while the
 * list could still scroll; otherwise the screen would jump between both states. A pull down brings them back.
 *
 * @param list The state of the scrolled list; the list's modifier takes [connection] with `nestedScroll`.
 * @param toggleDistance How many pixels a finger scrolls one way before the controls change.
 */
@Stable
class ScrollControls internal constructor(private val list: LazyListState, private val toggleDistance: Float) {
    private var hidden by mutableStateOf(false)

    // The distance scrolled in the current direction: negative down the song, positive up.
    private var travel = 0f

    /** Whether the list is scrolled to its top. */
    val atTop by derivedStateOf { list.firstVisibleItemIndex == 0 && list.firstVisibleItemScrollOffset == 0 }

    /** Whether the controls show. */
    val shown by derivedStateOf { !hidden || (atTop && list.canScrollForward) }

    /** Follows the user's scrolls, without consuming any. */
    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source == NestedScrollSource.UserInput && available.y != 0f) onScroll(available.y)
            return Offset.Zero
        }
    }

    /** Brings the controls back, as when a field takes the focus or the list jumps to its top. */
    fun show() {
        hidden = false
        travel = 0f
    }

    private fun onScroll(delta: Float) {
        travel = if (travel * delta < 0) delta else travel + delta
        when {
            travel <= -toggleDistance && list.canScrollForward -> hidden = true
            travel >= toggleDistance -> hidden = false
        }
    }
}

/** The [ScrollControls] of [list], kept across recompositions. */
@Composable
fun rememberScrollControls(list: LazyListState): ScrollControls {
    val distance = with(LocalDensity.current) { ToggleDistance.toPx() }
    return remember(list, distance) { ScrollControls(list = list, toggleDistance = distance) }
}
