package com.griot.feature.editor.audio

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.griot.core.data.audio.ExternalMusicState
import com.griot.feature.editor.R

/**
 * Another app's music in the [PlayerCard]: its title, app and progress with the notification access; « Musique d'une
 * autre app » and play/pause only without it.
 */
@Composable
internal fun ExternalPlayer(
    music: ExternalMusicState,
    onPlayToggle: () -> Unit,
    onRewind: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = music.title ?: stringResource(R.string.external_title)
    val subtitle = if (music.controlled) {
        listOfNotNull(
            music.artist ?: music.app,
            stringResource(
                R.string.external_position,
                formatDuration(music.positionMillis),
                formatDuration(music.durationMillis),
            )
                .takeIf { music.durationMillis > 0 },
        ).joinToString(" · ")
    } else {
        stringResource(R.string.external_no_access)
    }
    PlayerCard(
        title = title,
        subtitle = subtitle,
        cover = music.cover,
        playing = music.playing,
        progress = fractionOf(music.positionMillis, music.durationMillis).takeIf { music.controlled },
        onPlayToggle = onPlayToggle,
        onRewind = onRewind.takeIf { music.controlled },
        modifier = modifier,
    )
}
