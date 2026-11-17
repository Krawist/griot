package com.griot.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.BuiltInGenre
import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind
import com.griot.core.data.model.Genre
import com.griot.core.design.format.text
import com.griot.core.design.theme.GriotTheme

/** The name of the folder: the author's, or « Archives » in the interface's language. */
@Composable
internal fun Folder.displayName(): String =
    if (kind == FolderKind.ARCHIVES) stringResource(R.string.library_archives) else name

/** The name of the genre: the author's, or the built-in one's in the interface's language. */
@Composable
internal fun Genre.displayName(): String = when (builtIn) {
    null -> name
    BuiltInGenre.AFRO -> stringResource(R.string.genre_afro)
    BuiltInGenre.RAP -> stringResource(R.string.genre_rap)
    BuiltInGenre.RNB -> stringResource(R.string.genre_rnb)
    BuiltInGenre.GOSPEL -> stringResource(R.string.genre_gospel)
    BuiltInGenre.POP -> stringResource(R.string.genre_pop)
    BuiltInGenre.OTHER -> stringResource(R.string.genre_other)
}

/** The soft fill and the colour of a folder's icon. */
@Composable
internal fun FolderColor.tint(): Pair<Color, Color> {
    val palette = GriotTheme.palette
    return when (this) {
        FolderColor.MAGENTA -> palette.accentSoft to palette.accentText
        FolderColor.TURQUOISE -> palette.syncSoft to palette.syncText
        FolderColor.ORANGE -> palette.aiSoft to palette.aiText
        FolderColor.YELLOW -> palette.yellow.copy(alpha = YELLOW_FILL_ALPHA) to palette.ink2
        FolderColor.NEUTRAL -> palette.track to palette.ink2
    }
}

private const val YELLOW_FILL_ALPHA = 0.22f

/**
 * What a card says of a song: « EP 2027, Brouillons · Afro · il y a 2 h ». [showFolders] adds its folders, or « Sans
 * dossier », where the songs of several folders mix.
 */
@Composable
internal fun LibraryItem.meta(showFolders: Boolean): String = listOfNotNull(
    if (showFolders) {
        folders.map {
            it.displayName()
        }.joinToString(separator = ", ").ifEmpty { stringResource(R.string.library_no_folder) }
    } else {
        null
    },
    genre?.displayName(),
    updated.text(),
).joinToString(separator = " · ")
