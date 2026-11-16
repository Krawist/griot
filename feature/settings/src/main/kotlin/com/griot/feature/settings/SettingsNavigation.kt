package com.griot.feature.settings

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.griot.core.design.component.PlaceholderAction
import com.griot.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object SettingsGraph

@Serializable
internal data object SettingsRoute

fun NavController.navigateToSettings() = navigate(SettingsGraph)

/** Settings: profile, sync, AI, texts (GRI-29). */
fun NavGraphBuilder.settingsGraph(onBack: () -> Unit) {
    navigation<SettingsGraph>(startDestination = SettingsRoute) {
        composable<SettingsRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.settings_title),
                subtitle = stringResource(R.string.settings_subtitle),
                actions = listOf(
                    PlaceholderAction(stringResource(R.string.settings_back), onBack),
                ),
            )
        }
    }
}
