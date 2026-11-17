package com.griot.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.griot.core.design.R as DesignR
import com.griot.core.design.format.text
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val SearchHeight = 46.dp
private val NewSongSize = 44.dp
private val NewSongElevation = 8.dp
private val ContinueHeight = 36.dp

// The greeting and « Tes chansons », then « + » for a new song.
@Composable
internal fun Header(authorName: String?, onNewSong: () -> Unit) {
    val palette = GriotTheme.palette
    val description = stringResource(R.string.library_new_song)
    Row(
        modifier = Modifier.padding(horizontal = GriotSpacing.xs),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s + 2.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (authorName != null) {
                Text(
                    text = stringResource(R.string.library_greeting, authorName),
                    style = GriotTheme.type.description,
                    color = palette.muted,
                )
            }
            Text(
                text = stringResource(R.string.library_title),
                style = GriotTheme.type.screenTitle,
                color = palette.ink,
            )
        }
        Box(
            modifier = Modifier
                .size(NewSongSize)
                .shadow(elevation = NewSongElevation, shape = CircleShape, spotColor = palette.accent)
                .clip(CircleShape)
                .background(palette.accentFill)
                .clickable(role = Role.Button, onClick = onNewSong)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(DesignR.drawable.ic_add), contentDescription = null, tint = palette.onAccent)
        }
    }
}

@Composable
internal fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.field)
    val placeholder = stringResource(R.string.library_search)
    val textStyle = GriotTheme.type.body.copy(color = palette.ink)
    // The field keeps what is typed: the query comes back from the songs' search a moment later, and showing that
    // one would move the cursor back while typing. A query that becomes empty, as « Effacer la recherche » does, clears
    // it: only the change, never the query lagging behind what is typed.
    var text by rememberSaveable { mutableStateOf(query) }
    LaunchedEffect(query) { if (query.isEmpty()) text = "" }
    BasicTextField(
        value = text,
        onValueChange = {
            text = it
            onQueryChange(it)
        },
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(palette.accent),
        modifier = Modifier
            .fillMaxWidth()
            .height(SearchHeight)
            .background(palette.glass, shape)
            .border(1.dp, palette.line, shape)
            .semantics { contentDescription = placeholder },
        decorationBox = { field ->
            Row(
                modifier = Modifier.padding(horizontal = GriotSpacing.m + 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp),
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_search),
                    contentDescription = null,
                    tint = palette.muted,
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (text.isEmpty()) Text(text = placeholder, style = textStyle, color = palette.muted)
                    field()
                }
            }
        },
    )
}

/** A label above a group of the home screen, in upper case. */
@Composable
internal fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = GriotTheme.type.sectionLabel,
        color = GriotTheme.palette.muted,
        modifier = modifier.padding(horizontal = GriotSpacing.xs),
    )
}

// « Dossiers », and « Tous les textes · 43 » on the right.
@Composable
internal fun FoldersTitle(songCount: Int, onAllTexts: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionLabel(text = stringResource(R.string.library_folders), modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(GriotRadius.control))
                .clickable(role = Role.Button, onClick = onAllTexts)
                .padding(horizontal = GriotSpacing.xs, vertical = GriotSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.library_all_texts, songCount),
                style = GriotTheme.type.smallStrong,
                color = GriotTheme.palette.accentText,
            )
            Icon(
                painter = painterResource(DesignR.drawable.ic_chevron),
                contentDescription = null,
                tint = GriotTheme.palette.accentText,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

// The last song touched, its first two verses and « Continuer à écrire ».
@Composable
internal fun ResumeCard(resume: ResumeSong, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.sheet)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.glass, shape)
            .border(1.dp, palette.line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(GriotSpacing.l),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s + 2.dp),
    ) {
        Text(
            text = stringResource(R.string.library_resume_meta, resume.item.updated.text()),
            style = GriotTheme.type.caption,
            color = palette.muted,
        )
        Text(
            text = resume.item.song.title.ifBlank { stringResource(R.string.library_untitled) },
            style = GriotTheme.type.songTitle,
            color = palette.ink,
        )
        if (resume.verses.isNotEmpty()) {
            Text(
                text = resume.verses.joinToString(separator = "\n"),
                style = GriotTheme.type.quote,
                color = palette.ink2,
            )
        }
        Box(
            modifier = Modifier
                .height(ContinueHeight)
                .clip(CircleShape)
                .background(palette.accentFill)
                .padding(horizontal = GriotSpacing.m + 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.library_resume_continue),
                style = GriotTheme.type.label,
                color = palette.onAccent,
            )
        }
    }
}

/** « Griot propose de ranger N textes », in the AI colour: opens the proposals. */
@Composable
internal fun TidyUpCard(count: Int, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.card)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.aiSoft, shape)
            .border(1.dp, palette.ai.copy(alpha = TIDY_LINE_ALPHA), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = GriotSpacing.m + 2.dp, vertical = GriotSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
    ) {
        Box(
            modifier = Modifier.size(TidyIcon).background(palette.ai, RoundedCornerShape(GriotRadius.control)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_ai),
                contentDescription = null,
                tint = palette.onAi,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pluralStringResource(R.plurals.tidy_card, count, count),
                style = GriotTheme.type.bodyStrong,
                color = palette.ink,
            )
            Text(text = stringResource(R.string.tidy_card_body), style = GriotTheme.type.caption, color = palette.ink2)
        }
        Icon(
            painter = painterResource(DesignR.drawable.ic_chevron),
            contentDescription = null,
            tint = palette.aiText,
            modifier = Modifier.size(18.dp),
        )
    }
}

private val TidyIcon = 36.dp
private const val TIDY_LINE_ALPHA = 0.3f
