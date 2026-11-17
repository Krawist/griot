package com.griot.feature.library

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.Folder
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val IconBox = 32.dp
private val IconRadius = 9.dp
private val CardHeight = 72.dp

/** A folder of the home grid: its icon in its colour, its name and its count of songs. */
@Composable
internal fun FolderCard(folder: Folder, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.card)
    val (fill, content) = folder.color.tint()
    Column(
        modifier = modifier
            .heightIn(min = CardHeight)
            .clip(shape)
            .background(palette.glass, shape)
            .border(1.dp, palette.line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = GriotSpacing.m + 2.dp, vertical = GriotSpacing.m),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        Box(
            modifier = Modifier.size(IconBox).background(fill, RoundedCornerShape(IconRadius)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_folder),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(16.dp),
            )
        }
        Column {
            Text(
                text = folder.displayName(),
                style = GriotTheme.type.bodyStrong,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = pluralStringResource(R.plurals.library_texts, folder.songCount, folder.songCount),
                style = GriotTheme.type.caption,
                color = palette.muted,
            )
        }
    }
}

/** « Nouveau dossier », at the end of the grid. */
@Composable
internal fun NewFolderCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.card)
    Row(
        modifier = modifier
            .heightIn(min = CardHeight)
            .clip(shape)
            .border(1.5.dp, palette.line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = GriotSpacing.m + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s + 2.dp),
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_add),
            contentDescription = null,
            tint = palette.accentText,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.library_new_folder),
            style = GriotTheme.type.label,
            color = palette.accentText,
        )
    }
}

/** The folders two by two, then « Nouveau dossier ». */
@Composable
internal fun FolderGrid(folders: List<Folder>, onOpenFolder: (String) -> Unit, onNewFolder: () -> Unit) {
    val cells: List<Folder?> = folders + null
    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp)) {
        cells.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp),
            ) {
                row.forEach { folder ->
                    val cell = Modifier.weight(1f)
                    if (folder == null) {
                        NewFolderCard(onClick = onNewFolder, modifier = cell)
                    } else {
                        FolderCard(folder = folder, onClick = { onOpenFolder(folder.id) }, modifier = cell)
                    }
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}
