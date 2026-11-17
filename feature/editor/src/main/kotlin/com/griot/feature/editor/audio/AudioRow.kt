package com.griot.feature.editor.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.AttachmentKind
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.R

private val IconBox = 44.dp
private val IconRadius = 12.dp

/**
 * One audio: its icon, its name, and what it is (« Fichier du téléphone · 3:20 », « Lien Spotify · s'ouvre dans
 * Spotify », « Fichier introuvable », « Sur un autre téléphone »).
 */
@Composable
internal fun AudioRow(item: AudioItem, playing: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.card)
    val attachment = item.attachment
    val (fill, content) = when (attachment.kind) {
        AttachmentKind.FILE -> palette.syncSoft to palette.syncText
        AttachmentKind.LINK -> palette.glassSelected to palette.ink2
        AttachmentKind.MEMO -> palette.accentSoft to palette.accentText
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.glass, shape)
            .border(1.dp, palette.line, shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = GriotSpacing.m + 2.dp, vertical = GriotSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
    ) {
        Box(
            modifier = Modifier.size(IconBox).background(fill, RoundedCornerShape(IconRadius)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(attachment.kind.iconRes()), contentDescription = null, tint = content)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = attachment.title,
                style = GriotTheme.type.bodyStrong,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.detail(),
                style = GriotTheme.type.caption,
                color = if (item.missing || !attachment.onThisPhone) palette.accentText else palette.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (attachment.kind != AttachmentKind.LINK && attachment.onThisPhone && !item.missing) {
            PlayButton(playing = playing, onClick = onClick)
        }
        if (attachment.kind == AttachmentKind.LINK) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_external),
                contentDescription = null,
                tint = palette.muted,
            )
        }
    }
}

// Listen, or pause what plays, in a round button.
@Composable
private fun PlayButton(playing: Boolean, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val description = stringResource(if (playing) R.string.audios_pause else R.string.audios_play)
    Box(
        modifier = Modifier
            .size(PlaySize)
            .clip(CircleShape)
            .background(if (playing) palette.accentFill else palette.accentSoft)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(if (playing) DesignR.drawable.ic_pause else DesignR.drawable.ic_play),
            contentDescription = null,
            tint = if (playing) palette.onAccent else palette.accentText,
            modifier = Modifier.size(16.dp),
        )
    }
}

private val PlaySize = 40.dp

@Composable
private fun AudioItem.detail(): String {
    val attachment = attachment
    val length = attachment.durationMillis?.let(::formatDuration)
    return when {
        attachment.kind == AttachmentKind.LINK -> {
            val service = LinkService.of(attachment.uri).label
            if (service != null) {
                stringResource(R.string.audios_link_service, service)
            } else {
                stringResource(R.string.audios_link)
            }
        }
        !attachment.onThisPhone -> stringResource(R.string.audios_other_phone)
        missing -> stringResource(R.string.audios_missing)
        attachment.kind == AttachmentKind.MEMO -> listOfNotNull(
            stringResource(R.string.audios_memo),
            length,
        ).joinToString(" · ")
        else -> listOfNotNull(stringResource(R.string.audios_file), length).joinToString(" · ")
    }
}

/** « 3:20 » for 200 000 ms. */
internal fun formatDuration(millis: Long): String {
    val seconds = millis / MILLIS_PER_SECOND
    return "%d:%02d".format(seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE)
}

private const val MILLIS_PER_SECOND = 1_000
private const val SECONDS_PER_MINUTE = 60
