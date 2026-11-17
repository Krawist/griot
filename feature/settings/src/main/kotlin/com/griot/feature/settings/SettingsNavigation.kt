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
 * Settings: profile, sync, AI, texts, appearance and licences.
 *
 * @param onOpenImport Opens the import of texts.
 * @param onOpenTrash Opens the trash of the library.
 */
fun NavGraphBuilder.settingsGraph(onOpenImport: () -> Unit, onOpenTrash: () -> Unit) {
    navigation<SettingsGraph>(startDestination = SettingsRoute) {
        composable<SettingsRoute> { SettingsScreen(onOpenImport = onOpenImport, onOpenTrash = onOpenTrash) }
    }
}
