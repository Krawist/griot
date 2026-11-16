package com.griot.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Griot palette for one mode (light or dark), from the Wax colours. Magenta is the brand and main action colour,
 * orange is reserved for AI, turquoise for sync and states. Every text colour (`ink`, `ink2`, `muted` and the
 * `xxxText` colours) reaches 4.5:1 on [background], on [glass] and on its own soft fill. The rhyme colours are a
 * separate set, [RhymeColor].
 *
 * @property background Screen background: light ground in light mode, night ground in dark mode.
 * @property ink Main text and icons.
 * @property ink2 Secondary text that still reads as content: song previews, paragraphs, inactive chips.
 * @property muted Captions, hints, counts and inactive tabs.
 * @property glass Translucent fill of glass surfaces: cards, list rows, chips.
 * @property glassSelected Fill of a selected glass card.
 * @property glassBar Fill of the floating bars (bottom bar, editor toolbar), more opaque than [glass].
 * @property line Hairline border of glass surfaces and separators.
 * @property chip Opaque fill of small controls.
 * @property track Background of progress bars and sliders.
 * @property switchOff Track of a switch turned off.
 * @property radioOff Ring of an unselected radio button.
 * @property sheet Fill of bottom sheets.
 * @property scrim Dimming layer behind a sheet.
 * @property inverse Fill of a selected filter chip and other inverted controls.
 * @property onInverse Text drawn on [inverse].
 * @property selection Fill of the selected verse in the editor.
 * @property blobAlpha Opacity of the blurred colour shapes drawn behind a screen, from 0 to 1.
 * @property blobWarm Second colour of the blurred shapes of the logo and the launch screen: orange in light mode,
 * violet in dark mode. Magenta ([accent]) and turquoise ([sync]) are the others.
 * @property logoTile Ground of the logo tile, under its blurred shapes.
 * @property logoVeil Frosted veil drawn over the blurred shapes of the logo tile.
 * @property logoHighlight Hairline around the logo tile, catching the light.
 * @property accent Brand magenta: selection, active tab, icons, logo.
 * @property accentFill Magenta of filled buttons with [onAccent] text: a shade deeper than [accent] so that white
 * text reaches 4.5:1.
 * @property onAccent Text and icons drawn on [accentFill].
 * @property accentSoft Light magenta fill of selected chips and badges.
 * @property accentText Magenta text: links, secondary actions, section labels.
 * @property ai Orange of the AI actions, as a fill.
 * @property onAi Text and icons drawn on [ai].
 * @property aiSoft Light orange fill of AI chips and suggestions.
 * @property aiText Orange text of the AI actions.
 * @property sync Turquoise of sync and states, as a dot or an icon.
 * @property syncSoft Light turquoise fill of the sync chip.
 * @property syncText Turquoise text: sync state.
 * @property infoSoft Fill of information banners.
 * @property infoText Text of information banners.
 * @property yellow Sun yellow: the nib of the logo and small touches for ideas; the same in both modes.
 */
@Immutable
data class GriotPalette(
    val background: Color,
    val ink: Color,
    val ink2: Color,
    val muted: Color,
    val glass: Color,
    val glassSelected: Color,
    val glassBar: Color,
    val line: Color,
    val chip: Color,
    val track: Color,
    val switchOff: Color,
    val radioOff: Color,
    val sheet: Color,
    val scrim: Color,
    val inverse: Color,
    val onInverse: Color,
    val selection: Color,
    val blobAlpha: Float,
    val blobWarm: Color,
    val logoTile: Color,
    val logoVeil: Color,
    val logoHighlight: Color,
    val accent: Color,
    val accentFill: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val accentText: Color,
    val ai: Color,
    val onAi: Color,
    val aiSoft: Color,
    val aiText: Color,
    val sync: Color,
    val syncSoft: Color,
    val syncText: Color,
    val infoSoft: Color,
    val infoText: Color,
    val yellow: Color,
)

internal val LightPalette = GriotPalette(
    background = Color(0xFFF6F3F9),
    ink = Color(0xFF1E1B2E),
    ink2 = Color(0xFF4A4658),
    muted = Color(0xFF5E5A6B),
    glass = Color(0xADFFFFFF),
    glassSelected = Color(0xE6FFFFFF),
    glassBar = Color(0xD1FFFFFF),
    line = Color(0x1A1E1428),
    chip = Color(0xFFFFFFFF),
    track = Color(0xFFE4DEEA),
    switchOff = Color(0xFFD6D0DE),
    radioOff = Color(0xFFC9C2D3),
    sheet = Color(0xFFFFFFFF),
    scrim = Color(0x59140F1E),
    inverse = Color(0xFF1E1B2E),
    onInverse = Color(0xFFFFFFFF),
    selection = Color(0x14E0287D),
    blobAlpha = 0.16f,
    blobWarm = Color(0xFFFF7A1A),
    logoTile = Color(0xFFF4F1F8),
    logoVeil = Color(0x73FFFFFF),
    logoHighlight = Color(0xF2FFFFFF),
    accent = Color(0xFFE0287D),
    accentFill = Color(0xFFDB2277),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0x1FE0287D),
    accentText = Color(0xFFB01A60),
    ai = Color(0xFFFF7A1A),
    onAi = Color(0xFF2B1300),
    aiSoft = Color(0x24FF7A1A),
    aiText = Color(0xFFA04800),
    sync = Color(0xFF00B3A6),
    syncSoft = Color(0x2400B3A6),
    syncText = Color(0xFF006D65),
    infoSoft = Color(0x1A00B3A6),
    infoText = Color(0xFF0B5550),
    yellow = Color(0xFFFFC93C),
)

internal val DarkPalette = GriotPalette(
    background = Color(0xFF0F1018),
    ink = Color(0xFFF4F1F8),
    ink2 = Color(0xFFC3BFD0),
    muted = Color(0xFFA9A6B8),
    glass = Color(0x0FFFFFFF),
    glassSelected = Color(0x1CFFFFFF),
    glassBar = Color(0xDB161721),
    line = Color(0x1CFFFFFF),
    chip = Color(0xFF1C1D29),
    track = Color(0xFF2A2A38),
    switchOff = Color(0xFF3A3A4A),
    radioOff = Color(0xFF4A4858),
    sheet = Color(0xFF1A1B26),
    scrim = Color(0x8C000000),
    inverse = Color(0xFFF4F1F8),
    onInverse = Color(0xFF0F1018),
    selection = Color(0x29E0287D),
    blobAlpha = 0.22f,
    blobWarm = Color(0xFF7B2FF7),
    logoTile = Color(0xFF14151F),
    logoVeil = Color(0x7314151F),
    logoHighlight = Color(0x2EFFFFFF),
    accent = Color(0xFFE0287D),
    accentFill = Color(0xFFDB2277),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0x38E0287D),
    accentText = Color(0xFFFF7AB6),
    ai = Color(0xFFFF7A1A),
    onAi = Color(0xFF2B1300),
    aiSoft = Color(0x33FF7A1A),
    aiText = Color(0xFFFF9B52),
    sync = Color(0xFF3FD9CC),
    syncSoft = Color(0x3300B3A6),
    syncText = Color(0xFF3FD9CC),
    infoSoft = Color(0x2900B3A6),
    infoText = Color(0xFF7FE3D9),
    yellow = Color(0xFFFFC93C),
)

internal val LocalGriotPalette = staticCompositionLocalOf { LightPalette }
