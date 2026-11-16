package com.griot.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Griot theme. Follows the phone's dark mode; screens read the full palette through [GriotTheme.palette]. */
@Composable
fun GriotTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            secondary = palette.ai,
            onSecondary = palette.onAi,
            tertiary = palette.sync,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.background,
            onSurface = palette.ink,
            onSurfaceVariant = palette.muted,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            secondary = palette.ai,
            onSecondary = palette.onAi,
            tertiary = palette.sync,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.background,
            onSurface = palette.ink,
            onSurfaceVariant = palette.muted,
        )
    }
    CompositionLocalProvider(LocalGriotPalette provides palette) {
        MaterialTheme(colorScheme = colorScheme, typography = GriotTypography, content = content)
    }
}

object GriotTheme {
    val palette: GriotPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalGriotPalette.current
}
