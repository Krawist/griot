package com.griot.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

@Serializable
data object SettingsGraph

@Serializable
internal data object SettingsRoute

/**
 * Settings: profile, sync, AI, texts (GRI-29); for now, the language of the interface and the trash.
 *
 * @param onOpenTrash Opens the trash of the library.
 */
fun NavGraphBuilder.settingsGraph(onOpenTrash: () -> Unit) {
    navigation<SettingsGraph>(startDestination = SettingsRoute) {
        composable<SettingsRoute> { SettingsScreen(onOpenTrash = onOpenTrash) }
    }
}
