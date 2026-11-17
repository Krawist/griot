package com.griot.feature.editor.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.R

private val StopSize = 76.dp
private val StopSquare = 26.dp
private val PauseSize = 52.dp
private val WaveHeight = 72.dp
private val BarWidth = 4.dp
private val MinBar = 6.dp
private const val BARS = 44

/**
 * « Enregistrer sa voix »: the time, the live waveform, what the memo is attached to, and « Annuler », « Arrêter »
 * (which keeps it) and « Pause ». Closing the sheet keeps the memo too: a recording is never lost by mistake.
 */
@Composable
internal fun RecordingSheet(songId: String, verseId: String?, onVerse: Boolean, onClosed: () -> Unit) {
    val viewModel = hiltViewModel<RecordingViewModel, RecordingViewModel.Factory>(
        key = "recording/$songId/$verseId/$onVerse",
        creationCallback = { factory -> factory.create(songId, verseId, onVerse) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val title = memoTitle(uiState.target)
    val stop = { viewModel.onStop(title = title, onDone = onClosed) }
    GriotSheet(onDismiss = stop) {
        if (uiState.failed) {
            Text(
                text = stringResource(R.string.recording_failed),
                style = GriotTheme.type.body,
                color = GriotTheme.palette.ink2,
            )
            return@GriotSheet
        }
        Clock(uiState = uiState)
        Waveform(levels = uiState.levels)
        Targets(uiState = uiState, onTarget = viewModel::onTarget)
        Controls(
            paused = uiState.paused,
            onCancel = {
                viewModel.onCancel()
                onClosed()
            },
            onStop = stop,
            onPauseToggle = viewModel::onPauseToggle,
        )
    }
}

// « Mémo · Refrain », the memo's name in the audios.
@Composable
private fun memoTitle(target: MemoTarget): String = stringResource(R.string.recording_memo_title, target.text())

// The red dot with « Enregistrement » or « En pause », and the time.
@Composable
private fun Clock(uiState: RecordingUiState) {
    val palette = GriotTheme.palette
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(if (uiState.paused) palette.muted else palette.accent, CircleShape),
            )
            Text(
                text = stringResource(if (uiState.paused) R.string.recording_paused else R.string.recording_running),
                style = GriotTheme.type.smallStrong,
                color = palette.accentText,
            )
        }
        Text(text = formatDuration(uiState.elapsedMillis), style = GriotTheme.type.timer, color = palette.ink)
    }
}

// The last loudness heard, as bars growing from the middle.
@Composable
private fun Waveform(levels: List<Float>) {
    val palette = GriotTheme.palette
    val bars = List(BARS - levels.size) { 0f } + levels
    Row(
        modifier = Modifier.fillMaxWidth().height(WaveHeight),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        bars.forEachIndexed { index, level ->
            Box(
                Modifier
                    .width(BarWidth)
                    .height(MinBar + (WaveHeight - MinBar) * level)
                    .background(
                        if (index >=
                            BARS - levels.size
                        ) {
                            palette.accent
                        } else {
                            palette.track
                        },
                        RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}

// « Rattacher à » and its chips.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Targets(uiState: RecordingUiState, onTarget: (MemoTarget) -> Unit) {
    val palette = GriotTheme.palette
    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
        Text(
            text = stringResource(R.string.recording_attach),
            style = GriotTheme.type.smallStrong,
            color = palette.ink2,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        ) {
            uiState.targets.forEach { target ->
                val selected = target == uiState.target
                Text(
                    text = target.text(),
                    style = GriotTheme.type.chip(selected = selected),
                    color = if (selected) palette.onInverse else palette.ink2,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selected) palette.inverse else palette.glass, CircleShape)
                        .then(if (selected) Modifier else Modifier.border(1.dp, palette.line, CircleShape))
                        .clickable(role = Role.RadioButton, onClick = { onTarget(target) })
                        .padding(horizontal = GriotSpacing.m + 2.dp, vertical = GriotSpacing.s),
                )
            }
        }
    }
}

// « Annuler », the stop button that keeps the memo, and « Pause » or « Reprendre ».
@Composable
private fun Controls(paused: Boolean, onCancel: () -> Unit, onStop: () -> Unit, onPauseToggle: () -> Unit) {
    val palette = GriotTheme.palette
    val stopDescription = stringResource(R.string.recording_stop)
    val pauseDescription = stringResource(if (paused) R.string.recording_resume else R.string.recording_pause)
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = GriotSpacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.recording_cancel),
                style = GriotTheme.type.textButton,
                color = palette.ink2,
                modifier = Modifier
                    .heightIn(min = GriotSpacing.touchTarget)
                    .clip(RoundedCornerShape(GriotSpacing.s))
                    .clickable(role = Role.Button, onClick = onCancel)
                    .padding(horizontal = GriotSpacing.m, vertical = GriotSpacing.m),
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(StopSize)
                    .border(6.dp, palette.accentSoft, CircleShape)
                    .clip(CircleShape)
                    .background(palette.accentFill)
                    .clickable(role = Role.Button, onClick = onStop)
                    .semantics { contentDescription = stopDescription },
                contentAlignment = Alignment.Center,
            ) { Box(Modifier.size(StopSquare).background(palette.onAccent, RoundedCornerShape(6.dp))) }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(PauseSize)
                    .clip(CircleShape)
                    .background(palette.glass)
                    .border(1.dp, palette.line, CircleShape)
                    .clickable(role = Role.Button, onClick = onPauseToggle)
                    .semantics { contentDescription = pauseDescription },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (paused) DesignR.drawable.ic_play else DesignR.drawable.ic_pause),
                    contentDescription = null,
                    tint = palette.ink,
                )
            }
        }
    }
}
