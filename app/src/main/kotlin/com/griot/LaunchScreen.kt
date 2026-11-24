package com.griot

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.griot.core.design.component.LogoTile
import com.griot.core.design.theme.GriotTheme
import io.github.krawist.design.component.BackdropShape
import io.github.krawist.design.component.backdrop
import kotlin.time.Duration.Companion.seconds

/** How long the launch screen stays before the onboarding or the library opens. */
internal val LaunchDuration = 2.seconds

// Same tile as the system splash (res/drawable/splash_icon.xml), so the hand-off does not move.
private val TileSize = 128.dp
private val WordmarkGap = 28.dp
private val WordmarkHeight = 50.dp
private val TaglineBottom = 56.dp

// Background shapes, as fractions of the screen size, from the launch screen mockup.
private const val MAGENTA_X = 0.13f
private const val MAGENTA_Y = 0.3f
private const val WARM_X = 0.92f
private const val WARM_Y = 0.5f
private const val TURQUOISE_X = 0.49f
private const val TURQUOISE_Y = 0.73f
private const val SHAPE_RADIUS = 0.5f

/** Launch screen: the logo tile in the centre of the screen, on the blurred shapes, the wordmark under it. */
@Composable
internal fun LaunchScreen(modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .backdrop(
                listOf(
                    BackdropShape(
                        color = palette.accent.copy(alpha = palette.blobAlpha),
                        x = MAGENTA_X,
                        y = MAGENTA_Y,
                        radius = SHAPE_RADIUS,
                    ),
                    BackdropShape(
                        color = palette.blobWarm.copy(alpha = palette.blobAlpha),
                        x = WARM_X,
                        y = WARM_Y,
                        radius = SHAPE_RADIUS,
                    ),
                    BackdropShape(
                        color = palette.sync.copy(alpha = palette.blobAlpha),
                        x = TURQUOISE_X,
                        y = TURQUOISE_Y,
                        radius = SHAPE_RADIUS,
                    ),
                ),
            ),
    ) {
        // A spacer as tall as the wordmark above the tile keeps the tile in the exact centre of the screen.
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WordmarkGap),
        ) {
            Spacer(Modifier.height(WordmarkHeight))
            LogoTile(size = TileSize)
            Box(modifier = Modifier.height(WordmarkHeight), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = GriotTheme.type.brand,
                    color = palette.ink,
                )
            }
        }
        Text(
            text = stringResource(R.string.launch_tagline),
            style = GriotTheme.type.description,
            color = palette.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = TaglineBottom),
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LaunchScreenPreview() {
    GriotTheme { LaunchScreen() }
}
