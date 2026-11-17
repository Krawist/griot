package com.griot.core.design.catalog

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotPalette
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.core.design.theme.LyricTextStyle

// Catalogue of the design tokens and components, in light and dark mode. Preview only: token names are not user text.

@Composable
private fun CatalogSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun CatalogSurface(content: @Composable () -> Unit) {
    Surface(color = GriotTheme.palette.background) {
        Column(
            modifier = Modifier.padding(GriotSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.xl),
        ) { content() }
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
    ) {
        Box(
            Modifier
                .size(GriotSpacing.touchTarget)
                .border(1.dp, GriotTheme.palette.line, RoundedCornerShape(GriotRadius.row))
                .background(color, RoundedCornerShape(GriotRadius.row)),
        )
        Text(text = name, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun GriotPalette.tokens(): List<Pair<String, Color>> = listOf(
    "background" to background,
    "ink" to ink,
    "ink2" to ink2,
    "muted" to muted,
    "glass" to glass,
    "glassSelected" to glassSelected,
    "glassBar" to glassBar,
    "line" to line,
    "chip" to chip,
    "track" to track,
    "switchOff" to switchOff,
    "radioOff" to radioOff,
    "sheet" to sheet,
    "scrim" to scrim,
    "shadow" to shadow,
    "inverse" to inverse,
    "onInverse" to onInverse,
    "selection" to selection,
    "blobWarm" to blobWarm,
    "logoTile" to logoTile,
    "logoVeil" to logoVeil,
    "logoHighlight" to logoHighlight,
    "accent" to accent,
    "accentFill" to accentFill,
    "onAccent" to onAccent,
    "accentSoft" to accentSoft,
    "accentText" to accentText,
    "ai" to ai,
    "onAi" to onAi,
    "aiSoft" to aiSoft,
    "aiText" to aiText,
    "sync" to sync,
    "syncSoft" to syncSoft,
    "syncText" to syncText,
    "infoSoft" to infoSoft,
    "infoText" to infoText,
    "yellow" to yellow,
)

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaletteCatalogPreview() {
    GriotTheme {
        CatalogSurface {
            CatalogSection(title = "Palette") {
                GriotTheme.palette.tokens().forEach { (name, color) -> Swatch(name = name, color = color) }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RhymeCatalogPreview() {
    GriotTheme {
        CatalogSurface {
            CatalogSection(title = "Rhymes") {
                GriotTheme.rhymeColors.forEachIndexed { index, rhyme ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
                    ) {
                        Text(
                            text = ('A' + index).toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = rhyme.letter,
                            modifier = Modifier.width(GriotSpacing.xl),
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("Je marche seul dans la v")
                                withStyle(
                                    SpanStyle(background = rhyme.highlight, textDecoration = TextDecoration.Underline),
                                ) { append("ille") }
                            },
                            style = LyricTextStyle,
                            color = GriotTheme.palette.ink,
                        )
                        Box(Modifier.size(width = 24.dp, height = 2.dp).background(rhyme.underline))
                    }
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TypographyCatalogPreview() {
    GriotTheme {
        val typography = MaterialTheme.typography
        CatalogSurface {
            CatalogSection(title = "Typography") {
                listOf(
                    "displaySmall" to typography.displaySmall,
                    "headlineLarge" to typography.headlineLarge,
                    "headlineMedium" to typography.headlineMedium,
                    "titleLarge" to typography.titleLarge,
                    "titleMedium" to typography.titleMedium,
                    "bodyLarge" to typography.bodyLarge,
                    "bodyMedium" to typography.bodyMedium,
                    "bodySmall" to typography.bodySmall,
                    "labelLarge" to typography.labelLarge,
                    "labelMedium" to typography.labelMedium,
                    "labelSmall" to typography.labelSmall,
                    "lyric" to LyricTextStyle,
                ).forEach { (name, style) ->
                    Text(text = "$name · Écris tes chansons", style = style, color = GriotTheme.palette.ink)
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShapeAndSpacingCatalogPreview() {
    val radii: List<Pair<String, Dp>> = listOf(
        "bar" to GriotRadius.bar,
        "sheet" to GriotRadius.sheet,
        "card" to GriotRadius.card,
        "field" to GriotRadius.field,
        "row" to GriotRadius.row,
        "control" to GriotRadius.control,
        "badge" to GriotRadius.badge,
    )
    val spacings: List<Pair<String, Dp>> = listOf(
        "xs" to GriotSpacing.xs,
        "s" to GriotSpacing.s,
        "m" to GriotSpacing.m,
        "l" to GriotSpacing.l,
        "xl" to GriotSpacing.xl,
        "xxl" to GriotSpacing.xxl,
        "touchTarget" to GriotSpacing.touchTarget,
    )
    GriotTheme {
        CatalogSurface {
            CatalogSection(title = "Radii") {
                radii.forEach { (name, radius) ->
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(56.dp)
                            .border(1.dp, GriotTheme.palette.line, RoundedCornerShape(radius))
                            .background(GriotTheme.palette.glass, RoundedCornerShape(radius)),
                        contentAlignment = Alignment.Center,
                    ) { Text(text = "$name $radius", style = MaterialTheme.typography.labelMedium) }
                }
            }
            CatalogSection(title = "Spacing") {
                spacings.forEach { (name, spacing) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
                    ) {
                        Box(Modifier.size(width = spacing, height = 16.dp).background(GriotTheme.palette.accent))
                        Text(text = "$name $spacing", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
