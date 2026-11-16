package com.griot.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.griot.core.design.R

// Outfit and Inter variable fonts (OFL, licences in licenses/): one file each, one instance per weight used.
@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Outfit: titles and lyrics. */
private val OutfitFontFamily = FontFamily(
    variableFont(R.font.outfit, FontWeight.Normal),
    variableFont(R.font.outfit, FontWeight.Medium),
    variableFont(R.font.outfit, FontWeight.SemiBold),
    variableFont(R.font.outfit, FontWeight.Bold),
)

/** Inter: the interface. */
private val InterFontFamily = FontFamily(
    variableFont(R.font.inter, FontWeight.Normal),
    variableFont(R.font.inter, FontWeight.Medium),
    variableFont(R.font.inter, FontWeight.SemiBold),
)

private fun outfit(size: Int, weight: FontWeight, letterSpacing: TextUnit = 0.em, lineHeight: Float? = null) =
    TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight?.em ?: TextUnit.Unspecified,
    )

private fun inter(size: Int, weight: FontWeight, letterSpacing: TextUnit = 0.em, lineHeight: Float? = null) = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    letterSpacing = letterSpacing,
    lineHeight = lineHeight?.em ?: TextUnit.Unspecified,
)

/**
 * Type scale of the mockups. Display and headlines (Outfit) are for the onboarding and screen titles, titles (Outfit)
 * for song titles and cards; body and labels (Inter) for the interface. Lyrics use [LyricTextStyle].
 */
val GriotTypography = Typography(
    displaySmall = outfit(size = 34, weight = FontWeight.Bold, letterSpacing = (-0.018).em, lineHeight = 1.15f),
    headlineLarge = outfit(size = 32, weight = FontWeight.Bold, letterSpacing = (-0.015).em),
    headlineMedium = outfit(size = 26, weight = FontWeight.Bold, letterSpacing = (-0.015).em),
    titleLarge = outfit(size = 20, weight = FontWeight.Bold),
    titleMedium = outfit(size = 18, weight = FontWeight.SemiBold),
    bodyLarge = inter(size = 16, weight = FontWeight.Normal, lineHeight = 1.55f),
    bodyMedium = inter(size = 15, weight = FontWeight.Normal),
    bodySmall = inter(size = 14, weight = FontWeight.Normal, lineHeight = 1.45f),
    labelLarge = inter(size = 17, weight = FontWeight.SemiBold),
    labelMedium = inter(size = 14, weight = FontWeight.SemiBold),
    labelSmall = inter(size = 12, weight = FontWeight.SemiBold, letterSpacing = 0.03.em),
)

/** Lyrics in the editor: Outfit, at the default font size (GRI-16 scales it). */
val LyricTextStyle = outfit(size = 18, weight = FontWeight.Normal, lineHeight = 1.5f)
