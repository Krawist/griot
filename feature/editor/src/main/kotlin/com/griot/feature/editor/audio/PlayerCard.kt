package com.griot.feature.editor.audio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.data.audio.CoverArt
import com.griot.core.data.audio.PlayerColors
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.R

// The play button and the cover follow the player's corners: its radius minus its padding.
private val PlayerRadius = 24.dp
private val PlayerPadding = 10.dp
private val InnerRadius = PlayerRadius - PlayerPadding
private val CoverSize = 48.dp
private val PlayButton = 48.dp
private val ProgressHeight = 4.dp
private const val SUBTITLE_ALPHA = 0.72f
private const val TRACK_ALPHA = 0.22f
private const val GLOW_ALPHA = 0.35f
private const val SECOND_GLOW_ALPHA = 0.28f

// Where the glows sit, and how far they reach, in widths of the player.
private const val GLOW_X = 0.1f
private const val SECOND_GLOW_X = 0.9f
private const val GLOW_RADIUS = 0.45f

// Griot's own cover, in sizes of the cover: two coloured discs, then a record's hole and label.
private const val FIRST_DISC = 0.36f
private const val SECOND_DISC = 0.4f
private const val NEAR = 0.22f
private const val FAR = 0.82f
private const val HOLE = 0.26f
private const val LABEL = 0.17f

/**
 * The player alone at the bottom of the editor, for the song's beat or another app's music: its cover, [title] and
 * [subtitle], « Reculer de 15 secondes » when [onRewind] is given, play/pause, and the progress when [progress] is
 * known; [onTitleClick], when given, opens a choice from the title. Its colours come from the [cover]; white stays
 * readable on them, in light and dark modes alike.
 */
@Suppress("LongParameterList", "LongMethod") // What the player shows, and its controls.
@Composable
internal fun PlayerCard(
    title: String,
    subtitle: String,
    cover: CoverArt?,
    playing: Boolean,
    progress: Float?,
    onPlayToggle: () -> Unit,
    onRewind: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onTitleClick: (() -> Unit)? = null,
) {
    val colors = cover?.colors ?: PlayerColors.Griot
    val background = Color(colors.background)
    val glow = Color(colors.glow)
    val secondGlow = Color(colors.secondGlow)
    val shape = RoundedCornerShape(PlayerRadius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(glow.copy(alpha = GLOW_ALPHA), Color.Transparent),
                        center = Offset(size.width * GLOW_X, 0f),
                        radius = size.width * GLOW_RADIUS,
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(secondGlow.copy(alpha = SECOND_GLOW_ALPHA), Color.Transparent),
                        center = Offset(size.width * SECOND_GLOW_X, size.height),
                        radius = size.width * GLOW_RADIUS,
                    ),
                )
            }
            .padding(start = GriotSpacing.m, top = PlayerPadding, end = GriotSpacing.m, bottom = GriotSpacing.m),
        verticalArrangement = Arrangement.spacedBy(PlayerPadding),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
        ) {
            Cover(cover = cover)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (onTitleClick !=
                            null
                        ) {
                            Modifier.clickable(role = Role.Button, onClick = onTitleClick)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Text(
                    text = title,
                    style = GriotTheme.type.bodyStrong,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = GriotTheme.type.caption,
                    color = Color.White.copy(alpha = SUBTITLE_ALPHA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onRewind != null) Rewind(onClick = onRewind)
            PlayPause(playing = playing, background = background, onClick = onPlayToggle)
        }
        if (progress != null) Progress(fraction = progress)
    }
}

/** The song's beat in the [PlayerCard]. */
@Composable
internal fun BeatPlayer(
    beat: BeatUiState,
    onPlayToggle: () -> Unit,
    onRewind: () -> Unit,
    modifier: Modifier = Modifier,
    onChoose: (() -> Unit)? = null,
) {
    PlayerCard(
        title = beat.title,
        subtitle = stringResource(
            if (onChoose != null) R.string.player_position_choose else R.string.player_position,
            formatDuration(beat.positionMillis),
            formatDuration(beat.durationMillis),
        ),
        cover = beat.cover,
        playing = beat.playing,
        progress = fractionOf(beat.positionMillis, beat.durationMillis),
        onPlayToggle = onPlayToggle,
        onRewind = onRewind,
        modifier = modifier,
        onTitleClick = onChoose,
    )
}

/** How far [position] is in [duration], from 0 to 1; 0 while the length is unknown. */
internal fun fractionOf(position: Long, duration: Long): Float =
    if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f

// The embedded artwork, or Griot's own: two discs in the beat's colours and a yellow centre, like a record.
@Composable
private fun Cover(cover: CoverArt?) {
    val shape = RoundedCornerShape(InnerRadius)
    val image = cover?.image
    val imageBitmap = remember(image) { image?.asImageBitmap() }
    Box(modifier = Modifier.size(CoverSize).clip(shape)) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val colors = cover?.colors ?: PlayerColors.Griot
            val disc = Color(colors.background)
            val first = Color(colors.glow)
            val second = Color(colors.secondGlow)
            val centre = GriotTheme.palette.yellow
            Canvas(Modifier.fillMaxSize().background(disc)) {
                val unit = size.minDimension
                drawCircle(first, radius = unit * FIRST_DISC, center = Offset(size.width * NEAR, size.height * NEAR))
                drawCircle(second, radius = unit * SECOND_DISC, center = Offset(size.width * FAR, size.height * FAR))
                drawCircle(disc, radius = unit * HOLE, center = center)
                drawCircle(centre, radius = unit * LABEL, center = center)
            }
        }
    }
}

// « Reculer de 15 secondes »: the turning arrow with « 15 » inside.
@Composable
private fun Rewind(onClick: () -> Unit) {
    val description = stringResource(R.string.player_rewind)
    Box(
        modifier = Modifier
            .size(GriotSpacing.touchTarget)
            .clip(RoundedCornerShape(GriotSpacing.touchTarget / 2))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = painterResource(DesignR.drawable.ic_rewind_15), contentDescription = null, tint = Color.White)
        Text(
            text = stringResource(R.string.player_rewind_seconds),
            style = GriotTheme.type.micro,
            color = Color.White,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

// A white square with rounded corners, its icon in the player's colour.
@Composable
private fun PlayPause(playing: Boolean, background: Color, onClick: () -> Unit) {
    val description = stringResource(if (playing) R.string.audios_pause else R.string.audios_play)
    Box(
        modifier = Modifier
            .size(PlayButton)
            .clip(RoundedCornerShape(InnerRadius))
            .background(Color.White)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(if (playing) DesignR.drawable.ic_pause else DesignR.drawable.ic_play),
            contentDescription = null,
            tint = background,
        )
    }
}

@Composable
private fun Progress(fraction: Float) {
    val shape = RoundedCornerShape(ProgressHeight / 2)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ProgressHeight)
            .clip(shape)
            .background(Color.White.copy(alpha = TRACK_ALPHA)),
    ) {
        Box(Modifier.fillMaxWidth(fraction).height(ProgressHeight).background(Color.White, shape))
    }
}
