package com.griot.feature.library

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.griot.feature.library.folder.FolderScreen
import com.griot.feature.library.trash.TrashScreen
import kotlinx.serialization.Serializable

@Serializable
data object LibraryGraph

@Serializable
internal data object LibraryRoute

// Outside the library's graph: it is not a tab, so the bottom bar goes down on it.
@Serializable
internal data object TrashRoute

/**
 * The songs of a folder, or every song.
 *
 * @property folderId Id of the folder, [RECENT_FOLDER_ID] for « Récents »; `null` for « Tous les textes ».
 */
@Serializable
internal data class FolderRoute(val folderId: String?)

/** Opens the trash, from the settings. */
fun NavController.navigateToTrash() = navigate(TrashRoute)

/**
 * Home screen, folders and the trash (GRI-12, GRI-13, GRI-56, GRI-57). The editor is opened through the callback,
 * never directly.
 *
 * @param onOpenSong Opens the song with this id in the editor, a new one included.
 * @param onBack Leaves the trash.
 * @param onControlsShownChange Whether the library's controls show while the songs scroll: the bottom bar follows them.
 */
fun NavGraphBuilder.libraryGraph(
    navController: NavController,
    onOpenSong: (String) -> Unit,
    onBack: () -> Unit,
    onControlsShownChange: (Boolean) -> Unit,
) {
    navigation<LibraryGraph>(startDestination = LibraryRoute) {
        composable<LibraryRoute> {
            LibraryScreen(
                onOpenSong = onOpenSong,
                onOpenFolder = { folderId -> navController.navigate(FolderRoute(folderId)) },
                onControlsShownChange = onControlsShownChange,
            )
        }
    }
    composable<TrashRoute> { TrashScreen(onBack = onBack) }
    composable<FolderRoute> { entry ->
        FolderScreen(
            folderId = entry.toRoute<FolderRoute>().folderId,
            onBack = onBack,
            onOpenSong = onOpenSong,
        )
    }
}
