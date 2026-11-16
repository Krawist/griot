package com.griot.feature.library

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.griot.core.design.component.PlaceholderAction
import com.griot.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object LibraryGraph

@Serializable
internal data object LibraryRoute

/** Library of songs (GRI-12, GRI-13). The editor is opened through the callback, never directly. */
fun NavGraphBuilder.libraryGraph(onNewSong: () -> Unit) {
    navigation<LibraryGraph>(startDestination = LibraryRoute) {
        composable<LibraryRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.library_title),
                subtitle = stringResource(R.string.library_subtitle),
                actions = listOf(
                    PlaceholderAction(stringResource(R.string.library_new_song), onNewSong),
                ),
            )
        }
    }
}
