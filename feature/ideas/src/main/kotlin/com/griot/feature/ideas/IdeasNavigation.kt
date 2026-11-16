package com.griot.feature.ideas

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.griot.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object IdeasGraph

@Serializable
internal data object IdeasRoute

/** Ideas box (GRI-38, GRI-39). */
fun NavGraphBuilder.ideasGraph() {
    navigation<IdeasGraph>(startDestination = IdeasRoute) {
        composable<IdeasRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.ideas_title),
                subtitle = stringResource(R.string.ideas_subtitle),
            )
        }
    }
}
