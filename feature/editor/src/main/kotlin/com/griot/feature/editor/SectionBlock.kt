package com.griot.feature.editor

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.griot.core.data.model.SongVerse
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.RollingNumber
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.core.design.theme.LyricTextStyle

private val MarginWidth = 24.dp

// Lighter, for a count from a word guessed by grapheme rules.
private const val GUESSED_ALPHA = 0.55f

/** A section of the editor: its header with its scheme, then its verses, each with its rhyme letter and its count. */
@Suppress("LongParameterList") // The section, how it shows, and how its verses are typed.
@Composable
internal fun SectionBlock(
    section: EditorSection,
    fontSize: Int,
    showRhymes: Boolean,
    focus: VerseFocus?,
    verseInput: VerseInput,
    onActions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GriotSpacing.xs)) {
        SectionHeader(section = section, showRhymes = showRhymes, onActions = onActions)
        section.section.verses.forEach { verse ->
            // A repeat shows the same verses as its source: only the source's field takes the focus.
            val ownFocus = focus?.takeIf { it.verseId == verse.id && section.section.repeatOf == null }
            key(verse.id) {
                VerseRow(
                    verse = verse,
                    sound = section.sounds.verses[verse.id],
                    fontSize = fontSize,
                    showRhymes = showRhymes,
                    focus = ownFocus,
                    input = verseInput,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(section: EditorSection, showRhymes: Boolean, onActions: () -> Unit) {
    val palette = GriotTheme.palette
    val label = section.label.text()
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = GriotSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.04.em),
            color = palette.accentText,
        )
        val tag = if (section.section.repeatOf != null) stringResource(R.string.editor_repeat_tag) else null
        val scheme = section.sounds.scheme.takeIf { showRhymes && it.isNotEmpty() }
        listOfNotNull(tag, scheme).forEach {
            Text(text = it, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = palette.muted)
        }
        Box(Modifier.weight(1f).height(1.dp).background(palette.line))
        IconButton(onClick = onActions, modifier = Modifier.size(GriotSpacing.touchTarget)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_more),
                contentDescription = stringResource(R.string.editor_section_actions, label),
                tint = palette.muted,
            )
        }
    }
}

@Suppress("LongParameterList") // The verse, its sound, how it shows and how it is typed.
@Composable
private fun VerseRow(
    verse: SongVerse,
    sound: VerseSound?,
    fontSize: Int,
    showRhymes: Boolean,
    focus: VerseFocus?,
    input: VerseInput,
) {
    // The margins scale with the lyrics, so the letters and counts stay aligned with them.
    val marginStyle = MaterialTheme.typography.labelSmall.copy(fontSize = (fontSize * MARGIN_RATIO).sp)
    val marginTop = (fontSize * TOP_RATIO).dp
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        VerseLetter(
            sound = sound,
            showRhymes = showRhymes,
            style = marginStyle,
            modifier = Modifier.width(MarginWidth).padding(top = marginTop),
        )
        SyncedTextField(
            text = verse.text,
            onTextChange = { input.onChange(verse.id, it) },
            // In sp: the system font scale still applies on top of the size chosen.
            style = LyricTextStyle.copy(fontSize = fontSize.sp),
            placeholder = stringResource(R.string.editor_verse_placeholder),
            modifier = Modifier.weight(1f).padding(vertical = GriotSpacing.xs),
            onLines = { lines, cursor -> input.onLines(verse.id, lines, cursor) },
            onBackspaceAtStart = { text -> input.onBackspaceAtStart(verse.id, text) },
            highlight = rhymeHighlight(sound = sound, showRhymes = showRhymes),
            onFocused = { input.onFocused(verse.id) },
            onCursor = { input.onCursor(verse.id, it) },
            focus = focus,
            onFocusHandled = input.onFocusHandled,
        )
        VerseCount(
            sound = sound.takeIf { verse.text.isNotBlank() },
            onLongPress = { input.onCountLongPress(verse.id) },
            style = marginStyle,
            modifier = Modifier.width(MarginWidth).padding(top = marginTop),
        )
    }
}

// Whether the verse is drawn in its rhyme's colour: rhymes shown, and another verse of the section shares its rhyme.
private fun VerseSound?.coloured(showRhymes: Boolean): Boolean = showRhymes && this?.rhymes == true && letter != null

// The rhyming ending in its rhyme's colours, which come in softly as the verse is typed; `null` when not coloured.
@Composable
private fun rhymeHighlight(sound: VerseSound?, showRhymes: Boolean): TextHighlight? {
    val coloured = sound.coloured(showRhymes)
    val rhyme = sound?.letter?.let { GriotTheme.rhymeColor(it) }
    val motion = GriotTheme.motion
    val background by animateColorAsState(
        targetValue = rhyme?.highlight?.takeIf { coloured } ?: Color.Transparent,
        animationSpec = motion.colorChange(),
        label = "rhyme background",
    )
    val underline by animateColorAsState(
        targetValue = rhyme?.underline?.takeIf { coloured } ?: Color.Transparent,
        animationSpec = motion.colorChange(),
        label = "rhyme underline",
    )
    val ending = sound?.ending?.takeIf { coloured } ?: return null
    return TextHighlight(range = ending, background = background, underline = underline)
}

// The rhyme letter in the margin, in its rhyme's colour when it rhymes, muted otherwise.
@Composable
private fun VerseLetter(sound: VerseSound?, showRhymes: Boolean, style: TextStyle, modifier: Modifier) {
    val letter = sound?.letter?.takeIf { showRhymes }
    val color = letter?.takeIf { sound.coloured(showRhymes) }?.let { GriotTheme.rhymeColor(it).letter }
    Text(
        text = letter?.let { letter(it).toString() }.orEmpty(),
        style = style,
        color = color ?: GriotTheme.palette.muted,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

// The syllables in the margin, rolling to their new count, lighter when a word was guessed: a long press then offers
// to correct it.
@Composable
private fun VerseCount(sound: VerseSound?, onLongPress: () -> Unit, style: TextStyle, modifier: Modifier) {
    val palette = GriotTheme.palette
    val guessed = sound?.count?.guessedWords.orEmpty().isNotEmpty()
    val press = if (guessed) Modifier.combinedClickable(onClick = {}, onLongClick = onLongPress) else Modifier
    Box(modifier = modifier.then(press), contentAlignment = Alignment.TopEnd) {
        if (sound != null) {
            RollingNumber(value = sound.count.syllables) { syllables ->
                Text(
                    text = syllables.toString(),
                    style = style.copy(fontWeight = FontWeight.Medium),
                    color = if (sound.count.guessedWords.isNotEmpty()) {
                        palette.muted.copy(
                            alpha = GUESSED_ALPHA,
                        )
                    } else {
                        palette.muted
                    },
                )
            }
        }
    }
}

// The margins' text is two thirds of the lyrics' size, and starts lower, on the lyrics' first line.
private const val MARGIN_RATIO = 0.66f
private const val TOP_RATIO = 0.55f
