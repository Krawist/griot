package com.griot.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
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
    CompositionLocalProvider(
        LocalGriotPalette provides palette,
        LocalRhymeColors provides rhymeColors,
        LocalGlassBlur provides glassBlur,
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = GriotTypography, content = content)
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

    /** Colour of the rhyme at [index] in order of appearance (A = 0); after the eighth, the colours start over. */
    @Composable
    @ReadOnlyComposable
    fun rhymeColor(index: Int): RhymeColor = rhymeColors[index.mod(rhymeColors.size)]
}

internal val LocalRhymeColors = staticCompositionLocalOf { LightRhymeColors }
