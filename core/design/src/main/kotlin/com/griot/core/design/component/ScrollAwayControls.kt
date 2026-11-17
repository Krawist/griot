package com.griot.core.design.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import com.griot.core.design.theme.GriotTheme
import kotlin.math.roundToInt

/**
 * The controls at the top of a scrolled screen (a header, a top bar), shown while [shown]. Hidden, they slide up and
 * give their height to the content below. They stay composed, so that they come back without a hitch; hidden, they
 * are not placed and take no touch.
 */
@Composable
fun TopControls(shown: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val progress by showProgress(shown)
    Box(
        modifier = modifier
            .clipToBounds()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minHeight = 0))
                val height = (placeable.height * progress).roundToInt()
                layout(placeable.width, height) {
                    if (height > 0) placeable.placeRelativeWithLayer(0, height - placeable.height) { alpha = progress }
                }
            },
    ) { content() }
}

/**
 * The controls floating at the bottom of a scrolled screen (a toolbar, the bottom bar), shown while [shown]. Hidden,
 * they slide down and fade out. They stay composed, so that they come back without a hitch; hidden, they are not
 * placed and take no touch.
 */
@Composable
fun BottomControls(shown: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val progress by showProgress(shown)
    Box(
        modifier = modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                if (progress > 0f) {
                    placeable.placeRelativeWithLayer(0, 0) {
                        translationY = placeable.height * (1f - progress)
                        alpha = progress
                    }
                }
            }
        },
    ) { content() }
}

// From 0 (hidden) to 1 (shown), with the motion tokens: it snaps when animations are off.
@Composable
private fun showProgress(shown: Boolean) = animateFloatAsState(
    targetValue = if (shown) 1f else 0f,
    animationSpec = GriotTheme.motion.tween(),
    label = "controls",
)
