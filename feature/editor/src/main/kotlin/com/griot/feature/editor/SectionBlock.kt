package com.griot.feature.editor

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.SongVerse
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotMenu
import com.griot.core.design.component.RollingNumber
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.core.phonetics.rhymeLetter

private val MarginWidth = 24.dp

/** The gap between two sections. */
internal val SectionGap = 22.dp

// Lighter, for a count from a word guessed by grapheme rules.
private const val GUESSED_ALPHA = 0.55f

/**
 * The items of [section] in the editor's list: its header with its scheme, then each verse as an item of its own, with
 * its rhyme letter and its count. A long section is then composed verse by verse as it scrolls in, not all at once.
 *
 * @param first Whether it is the first section: the others keep a gap above their header.
 */
@Suppress("LongParameterList") // The section, how it shows, and how its verses are typed.
internal fun LazyListScope.sectionItems(
    section: EditorSection,
    first: Boolean,
    fontSize: Int,
    showRhymes: Boolean,
    focus: VerseFocus?,
    selected: String?,
    verseInput: VerseInput,
    menuOpen: Boolean,
    onActions: () -> Unit,
    onMenuDismiss: () -> Unit,
    menu: @Composable ColumnScope.() -> Unit,
) {
    val id = section.section.id
    item(key = "section/$id", contentType = "section") {
        SectionHeader(
            section = section,
            showRhymes = showRhymes,
            menuOpen = menuOpen,
            onActions = onActions,
            onMenuDismiss = onMenuDismiss,
            menu = menu,
            modifier = Modifier.animateItem().padding(top = if (first) 0.dp else SectionGap),
        )
    }
    // A repeat shows the same verses as its source: the section's id keeps their keys apart.
    items(items = section.section.verses, key = { "verse/$id/${it.id}" }, contentType = { "verse" }) { verse ->
        VerseRow(
            verse = verse,
            sound = section.sounds.verses[verse.id],
            fontSize = fontSize,
            showRhymes = showRhymes,
            // Only the source's field takes the focus.
            focus = focus?.takeIf { it.verseId == verse.id && section.section.repeatOf == null },
            selected = verse.id == selected,
            input = verseInput,
            modifier = Modifier.animateItem().padding(top = GriotSpacing.xs),
        )
    }
}

// The label, the scheme, and the « ⋯ » button with the section's menu anchored to it.
@Suppress("LongParameterList") // The section, how it shows, and its menu.
@Composable
private fun SectionHeader(
    section: EditorSection,
    showRhymes: Boolean,
    menuOpen: Boolean,
    onActions: () -> Unit,
    onMenuDismiss: () -> Unit,
    menu: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = GriotTheme.palette
    val label = section.label.text()
    Row(
        modifier = modifier.fillMaxWidth().padding(start = GriotSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        Text(
            text = label.uppercase(),
            style = GriotTheme.type.sectionLabel,
            color = palette.accentText,
        )
        val tag = if (section.section.repeatOf != null) stringResource(R.string.editor_repeat_tag) else null
        val scheme = section.sounds.scheme.takeIf { showRhymes && it.isNotEmpty() }
        listOfNotNull(tag, scheme).forEach {
            Text(text = it, style = GriotTheme.type.caption, color = palette.muted)
        }
        Box(Modifier.weight(1f).height(1.dp).background(palette.line))
        Box {
            IconButton(onClick = onActions, modifier = Modifier.size(GriotSpacing.touchTarget)) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_more),
                    contentDescription = stringResource(R.string.editor_section_actions, label),
                    tint = palette.muted,
                )
            }
            GriotMenu(expanded = menuOpen, onDismiss = onMenuDismiss, content = menu)
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
    selected: Boolean,
    input: VerseInput,
    modifier: Modifier = Modifier,
) {
    val marginTop = (fontSize * TOP_RATIO).dp
    val focusManager = LocalFocusManager.current
    // The letter and the count select the verse without the keyboard: the field gives up its focus.
    val select = {
        focusManager.clearFocus()
        input.onSelect(verse.id)
    }
    Row(modifier = modifier.fillMaxWidth().selected(selected), verticalAlignment = Alignment.Top) {
        VerseLetter(
            sound = sound,
            showRhymes = showRhymes,
            style = GriotTheme.type.rhymeLetter(lyricsSize = fontSize),
            modifier = Modifier
                .width(MarginWidth)
                .clickable(interactionSource = null, indication = null, onClick = select)
                .padding(top = marginTop),
        )
        SyncedTextField(
            text = verse.text,
            onTextChange = { input.onChange(verse.id, it) },
            // In sp: the system font scale still applies on top of the size chosen.
            style = GriotTheme.type.lyrics(size = fontSize),
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
            onClick = select,
            onLongPress = { input.onCountLongPress(verse.id) },
            style = GriotTheme.type.syllableCount(lyricsSize = fontSize),
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
        text = letter?.let { rhymeLetter(it).toString() }.orEmpty(),
        style = style,
        color = color ?: GriotTheme.palette.muted,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

// The syllables in the margin, rolling to their new count, lighter when a word was guessed: a long press then offers
// to correct it.
@Composable
private fun VerseCount(
    sound: VerseSound?,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    style: TextStyle,
    modifier: Modifier,
) {
    val palette = GriotTheme.palette
    val guessed = sound?.count?.guessedWords.orEmpty().isNotEmpty()
    val press = Modifier.combinedClickable(
        interactionSource = null,
        indication = null,
        onClick = onClick,
        onLongClick = onLongPress.takeIf { guessed },
    )
    Box(modifier = modifier.then(press), contentAlignment = Alignment.TopEnd) {
        if (sound != null) {
            RollingNumber(value = sound.count.syllables) { syllables ->
                Text(
                    text = syllables.toString(),
                    style = style,
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

// The selected verse is circled, its letter and count included: a magenta outline on a light magenta fill.
@Composable
private fun Modifier.selected(selected: Boolean): Modifier {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(SelectionRadius)
    val fill by animateColorAsState(
        targetValue = if (selected) palette.selection else Color.Transparent,
        animationSpec = GriotTheme.motion.colorChange(),
        label = "selection fill",
    )
    val line by animateColorAsState(
        targetValue = if (selected) palette.accent.copy(alpha = SELECTION_LINE_ALPHA) else Color.Transparent,
        animationSpec = GriotTheme.motion.colorChange(),
        label = "selection line",
    )
    return background(fill, shape).border(SelectionLine, line, shape).padding(horizontal = GriotSpacing.xs)
}

private val SelectionRadius = 12.dp
private val SelectionLine = 1.5.dp
private const val SELECTION_LINE_ALPHA = 0.45f

// The margins' text starts lower, on the lyrics' first line.
private const val TOP_RATIO = 0.55f
