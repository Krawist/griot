package com.griot.feature.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.LogoTile
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import io.github.krawist.design.component.BackdropShape
import io.github.krawist.design.component.backdrop

private val LogoSize = 112.dp
private val WordmarkGap = 20.dp
private val HeadlineGap = 24.dp
private val ScreenPadding = 28.dp
private val ContentTop = 56.dp

// Background shapes, as fractions of the screen size, from the welcome mockup.
private const val MAGENTA_X = 0.13f
private const val MAGENTA_Y = 0.24f
private const val WARM_X = 0.92f
private const val WARM_Y = 0.42f
private const val TURQUOISE_X = 0.46f
private const val TURQUOISE_Y = 0.68f
private const val SHAPE_RADIUS = 0.54f

/**
 * First screen of the onboarding: the logo, the promise of Griot, « Commencer » and, when [onRestore] is set,
 * « Restaurer depuis Google Drive ».
 *
 * @param onRestore Opens the restore from Google Drive; `null` hides the link (until GRI-30).
 */
@Composable
internal fun WelcomeScreen(onStart: () -> Unit, onRestore: (() -> Unit)?, modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    // Its own shapes, over the app's: the onboarding has its own backdrop on every screen of the mockup.
    BoxWithConstraints(
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
            )
            .safeDrawingPadding(),
    ) {
        // Scrolls only with a large font; otherwise the promise sits in the middle of the room above the buttons.
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(start = ScreenPadding, top = ContentTop, end = ScreenPadding, bottom = ScreenPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier.height(0.dp))
            Promise()
            Actions(onStart = onStart, onRestore = onRestore)
        }
    }
}

@Composable
private fun Promise() {
    val palette = GriotTheme.palette
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        LogoTile(size = LogoSize)
        Spacer(Modifier.height(WordmarkGap))
        Text(
            text = stringResource(R.string.welcome_wordmark),
            style = GriotTheme.type.wordmark,
            color = palette.ink,
        )
        Spacer(Modifier.height(WordmarkGap + HeadlineGap))
        Text(
            text = stringResource(R.string.welcome_headline),
            style = GriotTheme.type.display,
            color = palette.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(WordmarkGap))
        Text(
            text = stringResource(R.string.welcome_body),
            style = GriotTheme.type.bodyLarge,
            color = palette.ink2,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Actions(onStart: () -> Unit, onRestore: (() -> Unit)?) {
    val palette = GriotTheme.palette
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GriotSpacing.l)) {
        GriotButton(text = stringResource(R.string.welcome_start), onClick = onStart)
        if (onRestore != null) {
            TextButton(
                onClick = onRestore,
                modifier = Modifier.fillMaxWidth().heightIn(min = GriotSpacing.touchTarget),
            ) {
                Text(
                    text = stringResource(R.string.welcome_restore),
                    style = GriotTheme.type.textButton,
                    color = palette.accentText,
                )
            }
        }
    }
}

@Preview(name = "Light", heightDp = 844, widthDp = 390)
@Preview(name = "Dark", heightDp = 844, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WelcomeScreenPreview() {
    GriotTheme { WelcomeScreen(onStart = {}, onRestore = {}) }
}
