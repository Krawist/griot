package com.griot.feature.library.folder

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.organisation.FolderRepository
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.EmptyAction
import com.griot.feature.library.EmptyState
import com.griot.feature.library.FAVORITES_FOLDER_ID
import com.griot.feature.library.R

/**
 * What a folder says without a song to show (« Vide · dossier », « Vide · aucun résultat »). Its filters left nothing:
 * « Effacer les filtres », and « Chercher dans les archives » when songs there would show. An empty folder of the
 * author: « Nouvelle chanson ici » and « Ranger des textes ici » (every text, to select and file). « Favoris »: how to
 * add one.
 */
@Composable
internal fun FolderEmptyState(uiState: FolderUiState, actions: FolderEmptyActions) {
    val palette = GriotTheme.palette
    val custom = uiState.folder != null && uiState.folder.id != FolderRepository.ARCHIVES_ID
    when {
        uiState.songCount > 0 -> EmptyState(
            icon = DesignR.drawable.ic_search,
            tint = palette.accent,
            title = stringResource(R.string.library_no_result_title),
            body = stringResource(R.string.folder_no_result_body),
            primary = EmptyAction(
                label = stringResource(R.string.folder_clear_filters),
                onClick = actions.onClearFilters,
            ),
            secondary = if (uiState.moreInArchives) {
                EmptyAction(label = stringResource(R.string.folder_search_archives), onClick = actions.onSearchArchives)
            } else {
                null
            },
        )
        uiState.virtual == FAVORITES_FOLDER_ID -> EmptyState(
            icon = DesignR.drawable.ic_heart,
            tint = palette.accent,
            title = stringResource(R.string.folder_empty_title),
            body = stringResource(R.string.folder_empty_favorites),
            secondary = EmptyAction(label = stringResource(R.string.folder_all_texts), onClick = actions.onOpenAll),
        )
        custom -> EmptyState(
            icon = DesignR.drawable.ic_folder,
            tint = palette.syncText,
            title = stringResource(R.string.folder_empty_waiting),
            body = stringResource(R.string.folder_empty_body),
            primary = EmptyAction(
                label = stringResource(R.string.folder_new_song_here),
                onClick = actions.onNewSongHere,
            ),
            secondary = EmptyAction(label = stringResource(R.string.folder_file_here), onClick = actions.onOpenAll),
        )
        else -> EmptyState(
            icon = DesignR.drawable.ic_folder,
            tint = palette.syncText,
            title = stringResource(R.string.folder_empty_title),
            body = stringResource(R.string.folder_empty_plain),
        )
    }
}
