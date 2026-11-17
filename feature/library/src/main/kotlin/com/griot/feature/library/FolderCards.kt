package com.griot.feature.library

import androidx.annotation.DrawableRes
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
import com.griot.core.data.model.FolderColor
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val IconBox = 32.dp
private val IconRadius = 9.dp
private val CardHeight = 72.dp

/** A folder of the home grid: its icon in its colour, its [name] and its [count] of songs. */
@Suppress("LongParameterList") // The folder as it shows, and its action.
@Composable
internal fun FolderCard(
    name: String,
    count: Int,
    color: FolderColor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = DesignR.drawable.ic_folder,
) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.card)
    val (fill, content) = color.tint()
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
                painter = painterResource(icon),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(16.dp),
            )
        }
        Column {
            Text(
                text = name,
                style = GriotTheme.type.bodyStrong,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = pluralStringResource(R.plurals.library_texts, count, count),
                style = GriotTheme.type.caption,
                color = palette.muted,
            )
        }
    }
}

/**
 * « Récents » (the songs of the last 24 hours, [recentCount]) and « Favoris » ([favoriteCount]), then the folders, two
 * by two.
 */
@Composable
internal fun FolderGrid(folders: List<Folder>, recentCount: Int, favoriteCount: Int, onOpenFolder: (String) -> Unit) {
    // `null` cells are « Récents » and « Favoris », by their id.
    val cells: List<Pair<String, Folder?>> =
        listOf(RECENT_FOLDER_ID to null, FAVORITES_FOLDER_ID to null) + folders.map { it.id to it }
    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp)) {
        cells.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp),
            ) {
                row.forEach { (id, folder) ->
                    val cell = Modifier.weight(1f)
                    if (folder == null) {
                        val recent = id == RECENT_FOLDER_ID
                        FolderCard(
                            name = stringResource(if (recent) R.string.library_recent else R.string.library_favorites),
                            count = if (recent) recentCount else favoriteCount,
                            color = if (recent) FolderColor.TURQUOISE else FolderColor.MAGENTA,
                            icon = if (recent) DesignR.drawable.ic_history else DesignR.drawable.ic_heart,
                            onClick = { onOpenFolder(id) },
                            modifier = cell,
                        )
                    } else {
                        FolderCard(
                            name = folder.displayName(),
                            count = folder.songCount,
                            color = folder.color,
                            onClick = { onOpenFolder(folder.id) },
                            modifier = cell,
                        )
                    }
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}
