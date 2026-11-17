package com.griot.feature.editor

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.core.phonetics.rhymeLetter

private val SheetRadius = 26.dp
private val HandleWidth = 36.dp
private val HandleHeight = 5.dp
private val LetterSize = 24.dp
private val LetterRadius = 7.dp
private val OptionHeight = 52.dp
private val OptionRadius = 14.dp

// A drag of more than this opens or closes the options.
private val DragThreshold = 24.dp

/**
 * The sheet of the selected verse, at the bottom of the editor: its handle and a summary line, then, dragged up or
 * with a tap on the handle, its options. Dragged down, it goes back to its handle.
 */
@Suppress("LongParameterList") // The verse, whether it is open, and one callback per option.
@Composable
internal fun VerseSheet(
    selection: VerseSelection,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onRhymes: () -> Unit,
    onAiHelp: () -> Unit,
    onVersions: () -> Unit,
    onMemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius)
    var dragged by remember { mutableFloatStateOf(0f) }
    val threshold = with(LocalDensity.current) { DragThreshold.toPx() }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.sheet, shape)
            .border(1.dp, palette.line, shape)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { dragged += it },
                onDragStarted = { dragged = 0f },
                onDragStopped = {
                    if (dragged < -threshold) onExpandedChange(true)
                    if (dragged > threshold) onExpandedChange(false)
                },
            ),
    ) {
        Summary(selection = selection, expanded = expanded, onClick = { onExpandedChange(!expanded) })
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Options(onRhymes = onRhymes, onAiHelp = onAiHelp, onVersions = onVersions, onMemo = onMemo)
        }
    }
}

// « Rimes », « Aide IA », « Mémo sur ce vers » and « Versions du vers », two by two.
@Composable
private fun Options(onRhymes: () -> Unit, onAiHelp: () -> Unit, onVersions: () -> Unit, onMemo: () -> Unit) {
    val palette = GriotTheme.palette
    Column(
        modifier = Modifier.padding(
            start = GriotSpacing.m,
            end = GriotSpacing.m,
            bottom = GriotSpacing.m + 2.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
            Option(
                icon = DesignR.drawable.ic_rhymes,
                label = stringResource(R.string.editor_verse_rhymes),
                tint = palette.syncText,
                onClick = onRhymes,
            )
            Option(
                icon = DesignR.drawable.ic_ai,
                label = stringResource(R.string.editor_verse_ai),
                tint = palette.aiText,
                fill = palette.aiSoft,
                line = palette.ai.copy(alpha = AI_LINE_ALPHA),
                onClick = onAiHelp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
            Option(
                icon = DesignR.drawable.ic_mic,
                label = stringResource(R.string.editor_verse_memo),
                tint = palette.accentText,
                onClick = onMemo,
            )
            Option(
                icon = DesignR.drawable.ic_history,
                label = stringResource(R.string.editor_verse_versions),
                tint = palette.muted,
                onClick = onVersions,
            )
        }
    }
}

private const val AI_LINE_ALPHA = 0.35f

// The handle and « Couplet 1 · vers 2 · 9 syllabes », after the verse's rhyme letter in its colour.
@Composable
private fun Summary(selection: VerseSelection, expanded: Boolean, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val description = stringResource(if (expanded) R.string.editor_verse_collapse else R.string.editor_verse_expand)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(start = GriotSpacing.l, top = GriotSpacing.s, end = GriotSpacing.l, bottom = GriotSpacing.m),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = HandleWidth, height = HandleHeight)
                .background(palette.track, RoundedCornerShape(HandleHeight)),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        ) {
            Letter(letter = selection.letter)
            Text(
                text = selection.summary(),
                style = GriotTheme.type.description,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(
                    if (expanded) R.string.editor_verse_collapse_hint else R.string.editor_verse_hint,
                ),
                style = GriotTheme.type.caption,
                color = palette.muted,
            )
        }
    }
}

// The rhyme letter on its rhyme's soft colour; an empty muted box for a verse without one.
@Composable
private fun Letter(letter: Int?) {
    val palette = GriotTheme.palette
    val rhyme = letter?.let { GriotTheme.rhymeColor(it) }
    Box(
        modifier = Modifier
            .size(LetterSize)
            .background(rhyme?.highlight ?: palette.track, RoundedCornerShape(LetterRadius)),
        contentAlignment = Alignment.Center,
    ) {
        if (letter != null) {
            Text(
                text = rhymeLetter(letter).toString(),
                style = GriotTheme.type.captionStrong,
                color = rhyme?.letter ?: palette.muted,
            )
        }
    }
}

@Composable
private fun VerseSelection.summary(): String {
    val place = stringResource(R.string.editor_verse_place, section.text(), number)
    return syllables?.let {
        stringResource(R.string.editor_verse_summary, place, pluralStringResource(R.plurals.editor_syllables, it, it))
    } ?: place
}

@Suppress("LongParameterList") // The option, its colours and its action.
@Composable
private fun RowScope.Option(
    @DrawableRes icon: Int,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    fill: Color = GriotTheme.palette.glass,
    line: Color = GriotTheme.palette.line,
) {
    val shape = RoundedCornerShape(OptionRadius)
    Row(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = OptionHeight)
            .clip(shape)
            .background(fill, shape)
            .border(1.dp, line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = GriotSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s + 2.dp),
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = tint)
        Text(text = label, style = GriotTheme.type.label, color = GriotTheme.palette.ink)
    }
}
