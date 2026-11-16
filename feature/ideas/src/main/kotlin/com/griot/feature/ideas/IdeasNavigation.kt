package com.griot.feature.ideas

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

@Serializable
data object IdeasGraph

@Serializable
internal data object IdeasRoute

/**
 * Ideas box (GRI-38, GRI-39).
 *
 * @param onOpenSong Opens the song with this id in the editor: the song an idea is linked to, or a song started from
 * it.
 */
fun NavGraphBuilder.ideasGraph(onOpenSong: (String) -> Unit) {
    navigation<IdeasGraph>(startDestination = IdeasRoute) {
        composable<IdeasRoute> { IdeasScreen(onOpenSong = onOpenSong) }
    }
}
