package com.griot.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// TODO(GRI-02): embed Outfit (titles and lyrics) and Inter (interface), both OFL, in res/font and use them here.
private val TitleFontFamily = FontFamily.Default
private val InterfaceFontFamily = FontFamily.Default

val GriotTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.015).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = (-0.015).em,
    ),
    titleMedium = TextStyle(fontFamily = TitleFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = InterfaceFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelMedium = TextStyle(fontFamily = InterfaceFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
)
