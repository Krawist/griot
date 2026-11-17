package com.griot.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.SongLanguage
import com.griot.core.design.R as DesignR
import com.griot.core.design.glass.GlassSurface
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val DotSize = 8.dp
private val FavoriteSize = 16.dp
private const val MAX_DOTS = 8

/**
 * One song: its title, language, first verse, the rhymes of its first section, and its [meta] (folder, genre, time of
 * change).
 */
@Composable
internal fun SongCard(
    item: LibraryItem,
    meta: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = GriotTheme.palette
    val song = item.song
    GlassSurface(
        shape = RoundedCornerShape(GriotRadius.card),
        modifier = modifier.fillMaxWidth(),
        interaction = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        blur = false,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = GriotSpacing.l, vertical = GriotSpacing.m + 2.dp),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = song.title.ifBlank { stringResource(R.string.library_untitled) },
                    style = GriotTheme.type.cardTitle,
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (song.favorite) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_heart),
                        contentDescription = stringResource(R.string.library_favorite),
                        tint = palette.accent,
                        modifier = Modifier.size(FavoriteSize),
                    )
                }
                LanguageBadge(language = song.language)
            }
            Text(
                text = song.firstVerse ?: stringResource(R.string.library_no_text),
                style = GriotTheme.type.small,
                color = if (song.firstVerse == null) palette.muted else palette.ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RhymeDots(ranks = item.rhymes)
                Text(
                    text = meta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = GriotTheme.type.caption,
                    color = palette.muted,
                )
            }
        }
    }
}

@Composable
private fun LanguageBadge(language: SongLanguage) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.badge)
    Text(
        text = stringResource(language.badgeRes()),
        style = GriotTheme.type.tag,
        color = palette.ink2,
        modifier = Modifier.border(1.dp, palette.line, shape).padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

// The rhymes of the first section, one dot per verse coloured as in the editor; at most [MAX_DOTS].
@Composable
private fun RhymeDots(ranks: List<Int>) {
    if (ranks.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs)) {
        ranks.take(MAX_DOTS).forEach { rank ->
            Box(modifier = Modifier.size(DotSize).background(GriotTheme.rhymeColor(rank).underline, CircleShape))
        }
    }
}

private fun SongLanguage.badgeRes(): Int = when (this) {
    SongLanguage.FRENCH -> R.string.library_language_french
    SongLanguage.ENGLISH -> R.string.library_language_english
}
