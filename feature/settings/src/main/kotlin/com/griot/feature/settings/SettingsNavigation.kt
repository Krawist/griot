package com.griot.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

@Serializable
data object SettingsGraph

@Serializable
internal data object SettingsRoute

/** Settings: profile, sync, AI, texts (GRI-29); for now, the language of the interface. */
fun NavGraphBuilder.settingsGraph() {
    navigation<SettingsGraph>(startDestination = SettingsRoute) {
        composable<SettingsRoute> { SettingsScreen() }
    }
}
