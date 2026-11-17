package com.griot.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
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

private val OutfitFontFamily = FontFamily(
    variableFont(R.font.outfit, FontWeight.Normal),
    variableFont(R.font.outfit, FontWeight.Medium),
    variableFont(R.font.outfit, FontWeight.SemiBold),
    variableFont(R.font.outfit, FontWeight.Bold),
)

private val InterFontFamily = FontFamily(
    variableFont(R.font.inter, FontWeight.Normal),
    variableFont(R.font.inter, FontWeight.Medium),
    variableFont(R.font.inter, FontWeight.SemiBold),
    variableFont(R.font.inter, FontWeight.Bold),
)

private fun outfit(size: Float, weight: FontWeight, letterSpacing: TextUnit = 0.sp, lineHeight: Float? = null) =
    TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight?.em ?: TextUnit.Unspecified,
    )

private fun inter(size: Float, weight: FontWeight, letterSpacing: TextUnit = 0.sp, lineHeight: Float? = null) =
    TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight?.em ?: TextUnit.Unspecified,
    )

/**
 * Type scale of the mockups, read by every screen through [GriotTheme.type]: no screen sets a size or a weight.
 * Outfit for titles and lyrics, Inter for the interface. Section labels are written in upper case by their screen.
 */
@Immutable
@Suppress("TooManyFunctions") // One style per role of the mockups.
class GriotType internal constructor() {
    /** The promise of the welcome screen. */
    val display = outfit(size = 34f, weight = FontWeight.Bold, letterSpacing = (-0.6).sp, lineHeight = 1.15f)

    /** « Griot » on the launch screen. */
    val brand = outfit(size = 40f, weight = FontWeight.Bold, letterSpacing = (-0.8).sp)

    /** « Griot » next to the logo of the welcome screen. */
    val wordmark = outfit(size = 22f, weight = FontWeight.Bold, letterSpacing = (-0.4).sp)

    /** Screen titles: « Tes chansons », « Réglages », onboarding questions. */
    val screenTitle = outfit(size = 32f, weight = FontWeight.Bold, letterSpacing = (-0.5).sp)

    /** Title of a screen opened from another one, such as a folder. */
    val pageTitle = outfit(size = 26f, weight = FontWeight.Bold, letterSpacing = (-0.4).sp)

    /** Titles of sheets and of « Versions ». */
    val sheetTitle = outfit(size = 22f, weight = FontWeight.Bold)

    /** The song title in the editor's top bar. */
    val songTitle = outfit(size = 20f, weight = FontWeight.Bold)

    /** Song titles in lists and card titles. */
    val cardTitle = outfit(size = 18f, weight = FontWeight.SemiBold)

    /** The time of a recording, its digits all the same width. */
    val timer = outfit(
        size = 48f,
        weight = FontWeight.SemiBold,
        letterSpacing = (-1).sp,
    ).copy(fontFeatureSettings = "tnum")

    /** A language code in its badge (« FR »). */
    val code = outfit(size = 15f, weight = FontWeight.Bold)

    /** Lyrics at the default size; [lyrics] scales them. */
    val lyricsDefault = outfit(size = 18f, weight = FontWeight.Normal, lineHeight = 1.5f)

    /** The verse the AI panel works on. */
    val verseLarge = outfit(size = 19f, weight = FontWeight.Normal, lineHeight = 1.35f)

    /** A rhyming word offered by the AI panel. */
    val word = outfit(size = 16f, weight = FontWeight.Normal)

    /** A verse shown out of the editor: suggestions, ideas, versions. */
    val quote = outfit(size = 17f, weight = FontWeight.Normal, lineHeight = 1.4f)

    /** Main buttons and the titles of big choices. */
    val button = inter(size = 17f, weight = FontWeight.SemiBold)

    /** What is typed in a text field. */
    val field = inter(size = 17f, weight = FontWeight.Normal)

    /** The title of an option card (« Synchronisation automatique »). */
    val heading = inter(size = 16f, weight = FontWeight.SemiBold)

    /** Explanations under a screen title. */
    val bodyLarge = inter(size = 16f, weight = FontWeight.Normal, lineHeight = 1.55f)

    /** Body text. */
    val body = inter(size = 15f, weight = FontWeight.Normal)

    /** The title of a row or a card written in Inter. */
    val bodyStrong = inter(size = 15f, weight = FontWeight.SemiBold)

    /** Text buttons, links and the labels of settings rows and menu actions. */
    val textButton = inter(size = 15f, weight = FontWeight.Medium)

    /** Descriptions and hints. */
    val description = inter(size = 14f, weight = FontWeight.Normal, lineHeight = 1.45f)

    /** The label of a field and short strong texts. */
    val label = inter(size = 14f, weight = FontWeight.SemiBold)

    /** Secondary lines: a song's first verse, a subtitle, a note. */
    val small = inter(size = 13f, weight = FontWeight.Normal)

    /** Small actions on cards (« Restaurer », « Utiliser »). */
    val smallStrong = inter(size = 13f, weight = FontWeight.SemiBold)

    /** Small secondary actions (« Copier »). */
    val smallMedium = inter(size = 13f, weight = FontWeight.Medium)

    /** Metadata: times, counts, the line under a title. */
    val caption = inter(size = 12f, weight = FontWeight.Normal)

    /** Strong metadata: a step, a tag. */
    val captionStrong = inter(size = 12f, weight = FontWeight.SemiBold)

    /** Section labels and group titles, written in upper case. */
    val sectionLabel = inter(size = 12f, weight = FontWeight.SemiBold, letterSpacing = 0.4.sp)

    /** Tiny badges: the language of a song card. */
    val tag = inter(size = 11f, weight = FontWeight.SemiBold)

    /** A count on an icon. */
    val counter = inter(size = 10f, weight = FontWeight.Bold).copy(
        lineHeight = 16.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both,
        ),
    )

    /** Words stressed inside a text, such as what a suggestion changes. */
    val emphasis = SpanStyle(fontWeight = FontWeight.SemiBold)

    private val chipSelected = inter(size = 14f, weight = FontWeight.SemiBold)
    private val chipDefault = inter(size = 14f, weight = FontWeight.Medium)
    private val tabSelected = inter(size = 12f, weight = FontWeight.SemiBold)
    private val tabDefault = inter(size = 12f, weight = FontWeight.Medium)

    /** A filter or choice chip, SemiBold when [selected]. */
    fun chip(selected: Boolean): TextStyle = if (selected) chipSelected else chipDefault

    /** A tab of the bottom bar or of a toolbar, SemiBold when [selected]. */
    fun tab(selected: Boolean): TextStyle = if (selected) tabSelected else tabDefault

    /** Lyrics at [size] sp, the size chosen by the author (GRI-16). */
    fun lyrics(size: Int): TextStyle = lyricsDefault.copy(fontSize = size.sp)

    /** The rhyme letter in the margin, scaled with lyrics of [lyricsSize] sp so it stays aligned with them. */
    fun rhymeLetter(lyricsSize: Int): TextStyle = captionStrong.copy(fontSize = (lyricsSize * MARGIN_RATIO).sp)

    /** The syllable count in the margin, scaled with lyrics of [lyricsSize] sp. */
    fun syllableCount(lyricsSize: Int): TextStyle = caption.copy(fontSize = (lyricsSize * MARGIN_RATIO).sp)

    /** Material's slots, for the Material components that read them. */
    internal fun material() = Typography(
        displaySmall = display,
        headlineLarge = screenTitle,
        headlineMedium = pageTitle,
        headlineSmall = sheetTitle,
        titleLarge = songTitle,
        titleMedium = cardTitle,
        bodyLarge = bodyLarge,
        bodyMedium = body,
        bodySmall = description,
        labelLarge = button,
        labelMedium = label,
        labelSmall = captionStrong,
    )

    private companion object {
        // The margins' text is two thirds of the lyrics' size: 12 sp for lyrics of 18 sp.
        const val MARGIN_RATIO = 2f / 3f
    }
}
