package com.griot.core.design.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.griot.core.design.glass.LocalGlassBlur

/**
 * Griot theme. Follows the phone's dark mode; screens read the full palette through [GriotTheme.palette].
 *
 * @param glassBlur Whether glass surfaces blur what is behind them, from `supportsGlassBlur()` at startup; otherwise
 * they are opaque tinted surfaces.
 */
@Composable
fun GriotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    glassBlur: Boolean = false,
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val rhymeColors = if (darkTheme) DarkRhymeColors else LightRhymeColors
    // Material components (buttons, text fields) take the filled magenta, which carries white text.
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accentFill,
            onPrimary = palette.onAccent,
            secondary = palette.ai,
            onSecondary = palette.onAi,
            tertiary = palette.sync,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.background,
            onSurface = palette.ink,
            onSurfaceVariant = palette.muted,
            outline = palette.line,
        )
    } else {
        lightColorScheme(
            primary = palette.accentFill,
            onPrimary = palette.onAccent,
            secondary = palette.ai,
            onSecondary = palette.onAi,
            tertiary = palette.sync,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.background,
            onSurface = palette.ink,
            onSurfaceVariant = palette.muted,
            outline = palette.line,
        )
    }
    val context = LocalContext.current
    // « Supprimer les animations » sets the animator scale to 0: Griot then moves nothing.
    val motion = remember(context) {
        GriotMotion(
            reduced =
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f,
        )
    }
    CompositionLocalProvider(
        LocalGriotPalette provides palette,
        LocalGriotMotion provides motion,
        LocalRhymeColors provides rhymeColors,
        LocalGlassBlur provides glassBlur,
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = GriotMaterialTypography, content = content)
    }
}

object GriotTheme {
    val palette: GriotPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalGriotPalette.current

    /** The eight rhyme colours of the current mode, in order of appearance. */
    val rhymeColors: List<RhymeColor>
        @Composable
        @ReadOnlyComposable
        get() = LocalRhymeColors.current

    /** The type scale of the mockups: every text reads its style here. */
    val type: GriotType
        get() = GriotTypeScale

    /** The durations, easings and transitions of every animation. */
    val motion: GriotMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalGriotMotion.current

    /** Colour of the rhyme at [index] in order of appearance (A = 0); after the eighth, the colours start over. */
    @Composable
    @ReadOnlyComposable
    fun rhymeColor(index: Int): RhymeColor = rhymeColors[index.mod(rhymeColors.size)]
}

private val GriotTypeScale = GriotType()

private val GriotMaterialTypography = GriotTypeScale.material()

internal val LocalRhymeColors = staticCompositionLocalOf { LightRhymeColors }

internal val LocalGriotMotion = staticCompositionLocalOf { GriotMotion(reduced = false) }
