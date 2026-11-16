package com.griot.feature.library

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

@Serializable
data object LibraryGraph

@Serializable
internal data object LibraryRoute

/**
 * Library of songs (GRI-12, GRI-13). The editor is opened through the callback, never directly.
 *
 * @param onOpenSong Opens the song with this id in the editor, a new one included.
 */
fun NavGraphBuilder.libraryGraph(onOpenSong: (String) -> Unit) {
    navigation<LibraryGraph>(startDestination = LibraryRoute) {
        composable<LibraryRoute> { LibraryScreen(onOpenSong = onOpenSong) }
    }
}
