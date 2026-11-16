package com.griot.core.design.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

private val GlassBlurRadius = 24.dp
private val GlassBorderWidth = 1.dp

/**
 * Translucent "glass" surface for cards, the bottom bar and sheets: it blurs what is behind it inside a [GlassScreen].
 * Where blur is off (see `supportsGlassBlur`), or outside a [GlassScreen], it is an opaque surface tinted with the same
 * glass colour, with the same shape and border.
 *
 * @param modifier Layout of the surface (size, padding, alignment), applied outside its shape.
 * @param strong Whether to use the more opaque glass of the floating bars (bottom bar, editor toolbar).
 * @param elevation Height of the soft shadow under the surface, for surfaces that float above the content.
 * @param interaction What makes the surface react to touch (`clickable`, `selectable`…), applied inside its shape so
 * that the press feedback follows the corners. Passed in [modifier] instead, the feedback would be a rectangle.
 */
@Composable
fun GlassSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    strong: Boolean = false,
    elevation: Dp = 0.dp,
    interaction: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = GriotTheme.palette
    val tint = if (strong) palette.glassBar else palette.glass
    val hazeState = LocalGlassHazeState.current
    // Styles compare by identity: one per background and tint, kept across recompositions.
    val style = remember(palette.background, tint) {
        HazeBlurStyle {
            backgroundColor(palette.background)
            colorEffects(listOf(HazeColorEffect.tint(tint)))
            blurRadius(GlassBlurRadius)
            noiseFactor(0f)
        }
    }
    val fill = if (LocalGlassBlur.current && hazeState != null) {
        // The layers of the glass screen below this surface, as the zIndex of their sources orders them.
        Modifier.hazeBlur(input = HazeInput.Sources(hazeState), style = style)
    } else {
        Modifier.background(tint.compositeOver(palette.background))
    }
    Box(
        modifier = modifier
            // The fill is opaque (blurred backdrop or tinted fallback), so the shadow only shows around the surface.
            .shadow(elevation = elevation, shape = shape, ambientColor = palette.shadow, spotColor = palette.shadow)
            .clip(shape)
            .then(interaction)
            .then(fill)
            .border(width = GlassBorderWidth, color = palette.line, shape = shape),
        content = content,
    )
}
