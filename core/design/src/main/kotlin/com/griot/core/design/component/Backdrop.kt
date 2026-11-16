package com.griot.core.design.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.griot.core.design.theme.GriotTheme

// Shapes behind the screens, as on the library mockup: magenta at the top right, turquoise on the left.
private const val MAGENTA_X = 0.92f
private const val MAGENTA_Y = 0.08f
private const val MAGENTA_RADIUS = 0.6f
private const val TURQUOISE_X = 0.03f
private const val TURQUOISE_Y = 0.6f
private const val TURQUOISE_RADIUS = 0.55f

/**
 * Draws the blurred [shapes] behind the content. They are radial gradients, so they look the same on every phone,
 * with or without blur support.
 */
fun Modifier.backdrop(shapes: List<BackdropShape>): Modifier = drawBehind {
    shapes.forEach { shape ->
        val center = Offset(size.width * shape.x, size.height * shape.y)
        val radius = size.width * shape.radius
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(shape.color, shape.color.copy(alpha = 0f)),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
}

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
    )
}
