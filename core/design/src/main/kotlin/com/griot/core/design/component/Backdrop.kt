package com.griot.core.design.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.griot.core.design.theme.GriotTheme
import io.github.krawist.design.component.BackdropShape

// Shapes behind the screens, as on the library mockup: magenta at the top right, turquoise on the left, and a warm
// yellow low on the right, so that the middle of a screen has colour too.
private const val MAGENTA_X = 0.92f
private const val MAGENTA_Y = 0.08f
private const val MAGENTA_RADIUS = 0.6f
private const val TURQUOISE_X = 0.03f
private const val TURQUOISE_Y = 0.6f
private const val TURQUOISE_RADIUS = 0.55f
private const val YELLOW_X = 0.85f
private const val YELLOW_Y = 0.78f
private const val YELLOW_RADIUS = 0.5f

/** The shapes drawn behind the screens of the app, in the current mode's colours. */
@Composable
@ReadOnlyComposable
fun screenBackdropShapes(): List<BackdropShape> {
    val palette = GriotTheme.palette
    return listOf(
        BackdropShape(
            color = palette.accent.copy(alpha = palette.blobAlpha),
            x = MAGENTA_X,
            y = MAGENTA_Y,
            radius = MAGENTA_RADIUS,
        ),
        BackdropShape(
            color = palette.sync.copy(alpha = palette.blobAlpha),
            x = TURQUOISE_X,
            y = TURQUOISE_Y,
            radius = TURQUOISE_RADIUS,
        ),
        BackdropShape(
            color = palette.yellow.copy(alpha = palette.blobAlpha),
            x = YELLOW_X,
            y = YELLOW_Y,
            radius = YELLOW_RADIUS,
        ),
    )
}
