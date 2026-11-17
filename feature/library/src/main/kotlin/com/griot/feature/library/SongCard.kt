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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.design.format.text
import com.griot.core.design.glass.GlassSurface
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.core.phonetics.rhymeScheme

private val DotSize = 8.dp
private const val MAX_DOTS = 8

/** One song of the library: its title, language, first verse, the rhymes of its first section, and how far it is. */
@Composable
internal fun SongCard(item: LibraryItem, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
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
                    text = song.meta(scheme = rhymeScheme(item.rhymes), updated = item.updated.text()),
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

// « Brouillon · 1 section · hier », or « ABAB · 3 sections · il y a 2 h » for a song in progress.
@Composable
private fun LibrarySong.meta(scheme: String, updated: String): String {
    val sections = pluralStringResource(R.plurals.library_sections, sectionCount, sectionCount)
    val parts = listOfNotNull(
        stringResource(R.string.library_draft).takeIf { status == SongStatus.DRAFT },
        scheme.take(MAX_DOTS).takeIf { status != SongStatus.DRAFT && it.isNotEmpty() },
        sections,
        updated,
    )
    return parts.joinToString(separator = " · ")
}

private fun SongLanguage.badgeRes(): Int = when (this) {
    SongLanguage.FRENCH -> R.string.library_language_french
    SongLanguage.ENGLISH -> R.string.library_language_english
}
