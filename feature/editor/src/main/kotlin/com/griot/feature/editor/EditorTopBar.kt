package com.griot.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.SongLanguage
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotMenu
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/**
 * The editor's top bar: back, the song's title with its language under it, the microphone, the audios with their
 * count, and « ⋯ »
 * with the song's [menu] anchored to it.
 */
@Suppress("LongParameterList") // The song, its menu, and one callback per button.
@Composable
internal fun EditorTopBar(
    title: String,
    language: SongLanguage,
    languageDetected: Boolean,
    audioCount: Int,
    menuOpen: Boolean,
    onBack: () -> Unit,
    onAudios: () -> Unit,
    onRecord: () -> Unit,
    onMenu: () -> Unit,
    onMenuDismiss: () -> Unit,
    menu: @Composable ColumnScope.() -> Unit,
) {
    val palette = GriotTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = GriotSpacing.s, top = GriotSpacing.xs, end = GriotSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(GriotSpacing.touchTarget)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_back),
                contentDescription = stringResource(R.string.editor_back),
                tint = palette.ink,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.ifBlank { stringResource(R.string.editor_title_placeholder) },
                style = GriotTheme.type.songTitle,
                color = if (title.isBlank()) palette.muted else palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val code = stringResource(language.codeRes())
            Text(
                text = if (languageDetected) stringResource(R.string.editor_language_auto_detected, code) else code,
                style = GriotTheme.type.caption,
                color = palette.muted,
            )
        }
        IconButton(onClick = onRecord, modifier = Modifier.size(GriotSpacing.touchTarget)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_mic),
                contentDescription = stringResource(R.string.recording_record),
                tint = palette.accentText,
            )
        }
        AudiosButton(count = audioCount, onClick = onAudios)
        Box {
            IconButton(onClick = onMenu, modifier = Modifier.size(GriotSpacing.touchTarget)) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_more),
                    contentDescription = stringResource(R.string.editor_menu),
                    tint = palette.ink,
                )
            }
            GriotMenu(expanded = menuOpen, onDismiss = onMenuDismiss, content = menu)
        }
    }
}

// The audios of the song, with their count in a magenta badge when there are some.
@Composable
private fun AudiosButton(count: Int, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val description = stringResource(R.string.audios_count, count)
    Box {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(GriotSpacing.touchTarget).semantics { contentDescription = description },
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_headphones),
                contentDescription = null,
                tint = palette.ink,
            )
        }
        if (count > 0) {
            // A pill of 16 dp, its number centred on its one line.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 5.dp)
                    .height(BadgeSize)
                    .widthIn(min = BadgeSize)
                    .background(palette.accentFill, RoundedCornerShape(BadgeSize / 2))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    style = GriotTheme.type.counter,
                    color = palette.onAccent,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private val BadgeSize = 16.dp

/** The short code of a language: « FR », « EN ». */
private fun SongLanguage.codeRes(): Int = when (this) {
    SongLanguage.FRENCH -> R.string.editor_language_french
    SongLanguage.ENGLISH -> R.string.editor_language_english
}
