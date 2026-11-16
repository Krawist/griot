package com.griot.core.design.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.griot.core.design.theme.GriotMotion
import com.griot.core.design.theme.GriotTheme

/**
 * A count that rolls to its new [value], such as the syllables of a verse: the new one comes up from below when it
 * grew, down from above when it shrank, as a counter. [content] draws the count; it is drawn as is when motion is
 * reduced.
 */
@Composable
fun RollingNumber(
    value: Int,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable (Int) -> Unit,
) {
    val motion = GriotTheme.motion
    AnimatedContent(
        targetState = value,
        modifier = modifier,
        contentAlignment = contentAlignment,
        transitionSpec = {
            if (motion.reduced) {
                ContentTransform(EnterTransition.None, ExitTransition.None)
            } else {
                val up = targetState > initialState
                (
                    slideInVertically(motion.tween<IntOffset>(GriotMotion.MEDIUM, GriotMotion.Emphasized)) {
                        if (up) it else -it
                    } + fadeIn(motion.tween(GriotMotion.SHORT))
                    ).togetherWith(
                    slideOutVertically(motion.tween<IntOffset>(GriotMotion.SHORT, GriotMotion.Exit)) {
                        if (up) -it else it
                    } + fadeOut(motion.tween(GriotMotion.SHORT)),
                ).using(SizeTransform(clip = false))
            }
        },
        label = "rolling number",
    ) { content(it) }
}
