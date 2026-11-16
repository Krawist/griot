package com.griot.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Griot palette for one mode (light or dark), from the Wax colours. Magenta is the brand and main action colour,
 * orange is reserved for AI, turquoise for sync and states. The rhyme colours are a separate set (GRI-02, GRI-21).
 */
@Immutable
data class GriotPalette(
    val background: Color,
    val ink: Color,
    val ink2: Color,
    val muted: Color,
    val glass: Color,
    val glassBorder: Color,
    val chip: Color,
    val track: Color,
    val accent: Color,
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
)

val LightPalette = GriotPalette(
    background = Color(0xFFF6F3F9),
    ink = Color(0xFF1E1B2E),
    ink2 = Color(0xFF4A4658),
    muted = Color(0xFF5E5A6B),
    glass = Color(0xADFFFFFF),
    glassBorder = Color(0xE6FFFFFF),
    chip = Color(0xFFFFFFFF),
    track = Color(0xFFE4DEEA),
    accent = Color(0xFFE0287D),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0x1FE0287D),
    accentText = Color(0xFFB01A60),
    ai = Color(0xFFFF7A1A),
    onAi = Color(0xFF2B1300),
    aiSoft = Color(0x24FF7A1A),
    aiText = Color(0xFFB35200),
    sync = Color(0xFF00B3A6),
    syncSoft = Color(0x2400B3A6),
    syncText = Color(0xFF00786F),
)

val DarkPalette = GriotPalette(
    background = Color(0xFF0F1018),
    ink = Color(0xFFF4F1F8),
    ink2 = Color(0xFFC3BFD0),
    muted = Color(0xFFA9A6B8),
    glass = Color(0x0FFFFFFF),
    glassBorder = Color(0x1CFFFFFF),
    chip = Color(0xFF1C1D29),
    track = Color(0xFF2A2A38),
    accent = Color(0xFFE0287D),
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
)

val LocalGriotPalette = staticCompositionLocalOf { LightPalette }
