package com.griot.feature.editor.history

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.VersionSource
import com.griot.core.design.R as DesignR
import com.griot.core.design.format.RelativeTime
import com.griot.core.design.format.text
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.R
import com.griot.feature.editor.text
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private val DotSize = 12.dp
private val TabHeight = 36.dp
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/** The versions of a verse or of the whole song, newest first, each of which can be restored or copied. */
@Composable
internal fun HistoryScreen(
    songId: String,
    verseId: String?,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel<HistoryViewModel, HistoryViewModel.Factory>(
        key = "$songId/$verseId",
        creationCallback = { factory -> factory.create(songId, verseId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = GriotTheme.palette
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(
            modifier = Modifier.padding(start = GriotSpacing.s, top = GriotSpacing.xs, end = GriotSpacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(GriotSpacing.touchTarget)) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_back),
                    contentDescription = stringResource(R.string.history_back),
                    tint = palette.ink,
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.history_title),
                    style = GriotTheme.type.sheetTitle,
                    color = palette.ink,
                )
                Text(text = subtitle(uiState), style = GriotTheme.type.description, color = palette.muted)
            }
        }
        if (verseId != null) {
            ScopeTabs(selected = uiState.scope, onSelect = viewModel::onScopeSelected)
        }
        if (!uiState.loading) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = GriotSpacing.xl,
                    top = GriotSpacing.l,
                    end = GriotSpacing.xl,
                    bottom = GriotSpacing.xxl,
                ),
            ) {
                itemsIndexed(uiState.entries) { index, entry ->
                    TimelineItem(
                        entry = entry,
                        last = index == uiState.entries.lastIndex,
                        onRestore = { entry.versionId?.let(viewModel::onRestore) },
                    )
                }
            }
        }
    }
}

// « Les néons · Couplet 1, vers 2 », or the title alone for the whole song.
@Composable
private fun subtitle(uiState: HistoryUiState): String {
    val title = uiState.title.ifBlank { stringResource(R.string.editor_title_placeholder) }
    val section = uiState.verseSection
    val number = uiState.verseNumber
    return if (uiState.scope == HistoryScope.VERSE && section != null && number != null) {
        stringResource(R.string.history_subtitle_verse, title, section.text(), number)
    } else {
        title
    }
}

@Composable
private fun ScopeTabs(selected: HistoryScope, onSelect: (HistoryScope) -> Unit) {
    val palette = GriotTheme.palette
    Row(
        modifier = Modifier
            .padding(horizontal = GriotSpacing.xl, vertical = GriotSpacing.s)
            .fillMaxWidth()
            .height(TabHeight)
            .background(palette.track, CircleShape)
            .padding(3.dp)
            .selectableGroup(),
    ) {
        listOf(HistoryScope.VERSE to R.string.history_scope_verse, HistoryScope.SONG to R.string.history_scope_song)
            .forEach { (scope, label) ->
                val active = scope == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(if (active) palette.chip else palette.track)
                        .selectable(selected = active, role = Role.Tab, onClick = { onSelect(scope) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(label),
                        style = GriotTheme.type.chip(selected = active),
                        color = if (active) palette.ink else palette.ink2,
                    )
                }
            }
    }
}

@Composable
private fun TimelineItem(entry: HistoryEntry, last: Boolean, onRestore: () -> Unit) {
    val palette = GriotTheme.palette
    val current = entry.versionId == null
    Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m)) {
        // The dot of the version, and the line down to the next one.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(DotSize).fillMaxHeight(),
        ) {
            Box(
                Modifier
                    .padding(top = 3.dp)
                    .size(DotSize)
                    .background(if (current) palette.accent else palette.chip, CircleShape)
                    .border(2.dp, if (current) palette.accentSoft else palette.line, CircleShape),
            )
            if (!last) Box(Modifier.width(2.dp).weight(1f).background(palette.line))
        }
        Column(
            modifier = Modifier.weight(1f).padding(bottom = GriotSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        ) {
            Header(entry = entry)
            entry.lines.forEach { line -> Line(line = line, old = !current) }
            if (!current) Actions(entry = entry, onRestore = onRestore)
        }
    }
}

// « Actuelle », « il y a 2 h », « hier, 23:14 », with its badge.
@Composable
private fun Header(entry: HistoryEntry) {
    val palette = GriotTheme.palette
    val recorded = entry.recorded
    val time = entry.time
    // An old day gets its time of day: « hier, 23:14 ».
    val withTime = recorded is RelativeTime.Yesterday || recorded is RelativeTime.Day
    val whenText = when {
        recorded == null -> stringResource(R.string.history_now)
        withTime && time != null -> stringResource(R.string.history_when, recorded.text(), time.format(TimeFormat))
        else -> recorded.text()
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
        Text(
            text = whenText.replaceFirstChar { it.uppercase() },
            style = GriotTheme.type.smallMedium,
            color = palette.muted,
        )
        Badge(source = entry.source, current = entry.versionId == null)
    }
}

// The badge of a version: « En cours » for the text now, « Suggestion IA », « Restaurée », « Autre appareil ».
@Composable
private fun Badge(source: VersionSource?, current: Boolean) {
    val palette = GriotTheme.palette
    val label = when {
        current -> R.string.history_badge_now
        source == VersionSource.AI_SUGGESTION -> R.string.history_badge_ai
        source == VersionSource.RESTORE -> R.string.history_badge_restore
        source == VersionSource.SYNC_CONFLICT -> R.string.history_badge_conflict
        else -> return
    }
    val ai = source == VersionSource.AI_SUGGESTION
    Text(
        text = stringResource(label),
        style = GriotTheme.type.tag,
        color = when {
            current -> palette.accentText
            ai -> palette.aiText
            else -> palette.syncText
        },
        modifier = Modifier
            .background(
                when {
                    current -> palette.accentSoft
                    ai -> palette.aiSoft
                    else -> palette.syncSoft
                },
                RoundedCornerShape(GriotRadius.badge),
            )
            .padding(horizontal = GriotSpacing.s, vertical = 2.dp),
    )
}

@Composable
private fun Line(line: HistoryLine, old: Boolean) {
    val palette = GriotTheme.palette
    val label = line.label
    if (label != null) {
        Text(
            text = label.text().uppercase(),
            style = GriotTheme.type.sectionLabel,
            color = palette.accentText,
        )
        return
    }
    val highlight = GriotTheme.rhymeColor(1).highlight
    Text(
        text = buildAnnotatedString {
            line.segments.forEach { segment ->
                if (segment.changed) {
                    withStyle(
                        SpanStyle(
                            background = highlight,
                            textDecoration = if (old) TextDecoration.LineThrough else TextDecoration.None,
                        ),
                    ) { append(segment.text) }
                } else {
                    append(segment.text)
                }
            }
        },
        style = GriotTheme.type.quote,
        color = if (old) palette.ink2 else palette.ink,
    )
}

@Composable
private fun Actions(entry: HistoryEntry, onRestore: () -> Unit) {
    val palette = GriotTheme.palette
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val labels = entry.lines.mapNotNull { it.label }.map { it.text() }.iterator()
    val copied = entry.lines.joinToString(separator = "\n") { line ->
        if (line.label != null) "\n[${labels.next()}]" else line.segments.joinToString(separator = "") { it.text }
    }.trim()
    Row(horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
        TextButton(onClick = onRestore, colors = ButtonDefaults.textButtonColors(contentColor = palette.accentText)) {
            Text(
                text = stringResource(R.string.history_restore),
                style = GriotTheme.type.smallStrong,
            )
        }
        TextButton(
            onClick = { scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(copied, copied))) } },
            colors = ButtonDefaults.textButtonColors(contentColor = palette.ink2),
        ) {
            Text(
                text = stringResource(R.string.history_copy),
                style = GriotTheme.type.smallStrong,
            )
        }
    }
}
