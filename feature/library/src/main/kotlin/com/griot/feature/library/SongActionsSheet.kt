package com.griot.feature.library

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.organisation.FolderRepository
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.TextInputForm
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val TileHeight = 76.dp
private val RowHeight = 52.dp
private val IconBox = 32.dp
private val IconSize = 16.dp
private val BlockShape = RoundedCornerShape(18.dp)
private val IconShape = RoundedCornerShape(10.dp)

/**
 * What a long press on a song offers (« Feuille d'actions d'un texte »): its title and [meta]; « Ouvrir »,
 * « Ranger » and « Partager »; then the favourites, « Renommer » (the sheet turns into a title field),
 * « Dupliquer », its language, « Exporter en Markdown » and « Archives »; and « Supprimer », recoverable for 30 days.
 *
 * @param meta What the header says of the song: its folders and genres, its language, when it changed.
 */
@Composable
internal fun SongActionsSheet(
    song: LibrarySong,
    meta: String,
    onAction: (SongAction) -> Unit,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = GriotTheme.palette
    var renaming by rememberSaveable { mutableStateOf(false) }
    // A song in « Archives »: the action takes it out.
    val archived = FolderRepository.ARCHIVES_ID in song.folderIds
    GriotSheet(onDismiss = onDismiss) {
        Column(modifier = Modifier.padding(bottom = GriotSpacing.s), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = song.title.ifBlank { stringResource(R.string.library_untitled) },
                style = GriotTheme.type.songTitle,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = meta,
                style = GriotTheme.type.small,
                color = palette.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AnimatedContent(targetState = renaming, label = "song actions") { rename ->
            if (rename) {
                TextInputForm(
                    value = song.title,
                    label = stringResource(R.string.library_rename_label),
                    placeholder = stringResource(R.string.library_untitled),
                    confirmLabel = stringResource(R.string.library_rename),
                    onConfirm = onRename,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.m)) {
                    QuickActions(onAction = onAction)
                    ActionList(song = song, archived = archived, onAction = onAction, onRename = { renaming = true })
                    DeleteButton(onClick = { onAction(SongAction.DELETE) })
                }
            }
        }
    }
}

// « Ouvrir », « Ranger » and « Partager », side by side.
@Composable
private fun QuickActions(onAction: (SongAction) -> Unit) {
    val palette = GriotTheme.palette
    Row(horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
        listOf(
            Quick(
                SongAction.OPEN,
                R.string.library_open,
                DesignR.drawable.ic_edit,
                palette.accentSoft,
                palette.accentText,
            ),
            Quick(
                SongAction.FILE,
                R.string.library_file_short,
                DesignR.drawable.ic_folder,
                palette.syncSoft,
                palette.syncText,
            ),
            Quick(SongAction.SHARE, R.string.library_share, DesignR.drawable.ic_share, palette.track, palette.ink2),
        ).forEach { quick ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = TileHeight)
                    .clip(BlockShape)
                    .background(palette.glass, BlockShape)
                    .border(1.dp, palette.line, BlockShape)
                    .clickable(role = Role.Button) { onAction(quick.action) }
                    .padding(vertical = GriotSpacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            ) {
                ActionIcon(icon = quick.icon, fill = quick.fill, tint = quick.tint)
                Text(text = stringResource(quick.label), style = GriotTheme.type.smallStrong, color = palette.ink)
            }
        }
    }
}

// One of the quick actions, with the colours of its icon.
private data class Quick(
    val action: SongAction,
    val label: Int,
    @DrawableRes val icon: Int,
    val fill: Color,
    val tint: Color,
)

// The favourites, « Renommer », « Dupliquer », the language, « Exporter en Markdown » and « Archives », in one card.
@Composable
private fun ActionList(song: LibrarySong, archived: Boolean, onAction: (SongAction) -> Unit, onRename: () -> Unit) {
    val palette = GriotTheme.palette
    val language = stringResource(
        if (song.language ==
            SongLanguage.ENGLISH
        ) {
            R.string.library_language_english_name
        } else {
            R.string.library_language_french_name
        },
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BlockShape)
            .background(palette.glass, BlockShape)
            .border(1.dp, palette.line, BlockShape),
    ) {
        ActionRow(
            icon = DesignR.drawable.ic_heart,
            label = stringResource(
                if (song.favorite) R.string.library_remove_favorite else R.string.library_add_favorite,
            ),
            first = true,
            onClick = { onAction(SongAction.FAVORITE) },
        )
        ActionRow(icon = DesignR.drawable.ic_edit, label = stringResource(R.string.library_rename), onClick = onRename)
        ActionRow(
            icon = DesignR.drawable.ic_copy,
            label = stringResource(R.string.library_duplicate),
            onClick = { onAction(SongAction.DUPLICATE) },
        )
        ActionRow(
            icon = DesignR.drawable.ic_language,
            label = stringResource(R.string.library_language),
            hint = language,
            onClick = { onAction(SongAction.LANGUAGE) },
        )
        ActionRow(
            icon = DesignR.drawable.ic_export,
            label = stringResource(R.string.library_export),
            hint = stringResource(R.string.library_export_hint),
            onClick = { onAction(SongAction.EXPORT) },
        )
        ActionRow(
            icon = DesignR.drawable.ic_archive,
            label = stringResource(if (archived) R.string.library_unarchive else R.string.library_archive),
            onClick = { onAction(SongAction.ARCHIVE) },
        )
    }
}

@Composable
private fun ActionRow(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    hint: String? = null,
    first: Boolean = false,
) {
    val palette = GriotTheme.palette
    if (!first) HorizontalDivider(thickness = 1.dp, color = palette.line)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = GriotSpacing.m + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
    ) {
        ActionIcon(icon = icon, fill = palette.track, tint = palette.ink2)
        Text(text = label, style = GriotTheme.type.body, color = palette.ink, modifier = Modifier.weight(1f))
        hint?.let { Text(text = it, style = GriotTheme.type.small, color = palette.muted) }
    }
}

@Composable
private fun ActionIcon(@DrawableRes icon: Int, fill: Color, tint: Color) {
    Box(modifier = Modifier.size(IconBox).background(fill, IconShape), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(IconSize),
        )
    }
}

// « Supprimer · récupérable 30 jours »: the song goes to the trash.
@Composable
private fun DeleteButton(onClick: () -> Unit) {
    val palette = GriotTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .clip(BlockShape)
            .background(palette.accentSoft, BlockShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.library_delete_recoverable),
            style = GriotTheme.type.bodyStrong,
            color = palette.accentText,
        )
    }
}
