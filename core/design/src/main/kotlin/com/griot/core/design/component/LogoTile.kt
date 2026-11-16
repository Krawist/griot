package com.griot.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.griot.core.design.R
import com.griot.core.design.theme.GriotTheme

// Corner radius and shadow, as fractions of the tile size, from the logo mockup (240 px tile, 54 px radius).
private const val RADIUS_RATIO = 0.234f
private const val SHADOW_RATIO = 0.12f

// Opacity of the blurred shapes inside the tile, stronger than behind a screen.
private const val SHAPE_ALPHA = 0.45f

/**
 * The Griot logo: the quill and wave on a glass tile, frosted in light mode and night glass in dark mode, with the
 * blurred magenta, warm and turquoise shapes behind the veil. Drawn, not an image, so the blur works on every phone.
 *
 * @param size Side of the tile.
 */
@Composable
fun LogoTile(size: Dp, modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(size * RADIUS_RATIO)
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = size * SHADOW_RATIO, shape = shape)
            .clip(shape)
            .background(palette.logoTile)
            .backdrop(
                listOf(
                    BackdropShape(
                        color = palette.accent.copy(alpha = SHAPE_ALPHA),
                        x = 0.19f,
                        y = 0.22f,
                        radius = 0.5f,
                    ),
                    BackdropShape(
                        color = palette.blobWarm.copy(alpha = SHAPE_ALPHA),
                        x = 0.85f,
                        y = 0.45f,
                        radius = 0.45f,
                    ),
                    BackdropShape(color = palette.sync.copy(alpha = SHAPE_ALPHA), x = 0.51f, y = 0.9f, radius = 0.52f),
                ),
            )
            .background(palette.logoVeil)
            .border(width = 1.dp, color = palette.logoHighlight, shape = shape),
    ) {
        Image(
            painter = painterResource(R.drawable.logo_quill),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogoTilePreview() {
    GriotTheme {
        Box(Modifier.background(GriotTheme.palette.background)) {
            LogoTile(size = 128.dp)
        }
    }
}
