package com.griot.feature.editor.audio

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The song's [beat] in the editor's player, [onChoose] another when it has several ([choices]); without one, the
 * music another app plays; without either, no player.
 */
@Suppress("LongParameterList") // The beat, its choice, and the other app's music.
internal fun editorPlayer(
    beat: BeatUiState?,
    choices: Int,
    onChoose: () -> Unit,
    beatViewModel: BeatViewModel,
    external: ExternalMusicUiState,
    externalViewModel: ExternalMusicViewModel,
): (@Composable (Modifier) -> Unit)? = when {
    beat != null -> { modifier ->
        BeatPlayer(
            beat = beat,
            onPlayToggle = beatViewModel::onPlayToggle,
            onRewind = beatViewModel::onRewind,
            modifier = modifier,
            onChoose = onChoose.takeIf { choices > 1 },
        )
    }
    external.music.active -> { modifier ->
        ExternalPlayer(
            music = external.music,
            onPlayToggle = externalViewModel::onPlayToggle,
            onRewind = externalViewModel::onRewind,
            modifier = modifier,
        )
    }
    else -> null
}
