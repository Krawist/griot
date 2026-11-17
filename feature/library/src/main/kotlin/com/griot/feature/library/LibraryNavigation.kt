package com.griot.feature.library

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.griot.feature.library.trash.TrashScreen
import kotlinx.serialization.Serializable

@Serializable
data object LibraryGraph

@Serializable
internal data object LibraryRoute

// Outside the library's graph: it is not a tab, so the bottom bar goes down on it.
@Serializable
internal data object TrashRoute

/** Opens the trash, from the settings. */
fun NavController.navigateToTrash() = navigate(TrashRoute)

/**
 * Library of songs and its trash (GRI-12, GRI-13). The editor is opened through the callback, never directly.
 *
 * @param onOpenSong Opens the song with this id in the editor, a new one included.
 * @param onOpenFolder Opens the folder with this id, or every song when `null`.
 * @param onBack Leaves the trash.
 * @param onControlsShownChange Whether the library's controls show while the songs scroll: the bottom bar follows them.
 */
fun NavGraphBuilder.libraryGraph(
    onOpenSong: (String) -> Unit,
    onOpenFolder: (String?) -> Unit,
    onBack: () -> Unit,
    onControlsShownChange: (Boolean) -> Unit,
) {
    navigation<LibraryGraph>(startDestination = LibraryRoute) {
        composable<LibraryRoute> {
            LibraryScreen(
                onOpenSong = onOpenSong,
                onOpenFolder = onOpenFolder,
                onControlsShownChange = onControlsShownChange,
            )
        }
    }
    composable<TrashRoute> { TrashScreen(onBack = onBack) }
}
