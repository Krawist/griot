package com.griot.feature.onboarding

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.griot.feature.onboarding.importing.ImportScreen
import com.griot.feature.onboarding.language.LanguageScreen
import com.griot.feature.onboarding.name.NameScreen
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingGraph

@Serializable
internal data object WelcomeRoute

@Serializable
internal data object NameRoute

@Serializable
internal data object LanguageRoute

@Serializable
internal data object ImportRoute

// The import opened from the settings: outside the onboarding's graph, without its progress.
@Serializable
internal data object SettingsImportRoute

/** Opens the import of texts, from the settings. */
fun NavController.navigateToImport() = navigate(SettingsImportRoute)

/**
 * Welcome, name, language, sync and import (GRI-07 to GRI-11).
 *
 * @param onRestore Opens the restore from Google Drive, which skips the rest of the onboarding; `null` until GRI-30.
 */
fun NavGraphBuilder.onboardingGraph(navController: NavController, onCompleted: () -> Unit, onRestore: (() -> Unit)?) {
    navigation<OnboardingGraph>(startDestination = WelcomeRoute) {
        composable<WelcomeRoute> {
            WelcomeScreen(onStart = { navController.navigate(NameRoute) }, onRestore = onRestore)
        }
        composable<NameRoute> {
            NameScreen(onBack = { navController.popBackStack() }, onNext = { navController.navigate(LanguageRoute) })
        }
        // The sync step (GRI-10) comes between the language and the import.
        composable<LanguageRoute> {
            LanguageScreen(onBack = { navController.popBackStack() }, onNext = { navController.navigate(ImportRoute) })
        }
        composable<ImportRoute> { ImportScreen(onBack = { navController.popBackStack() }, onDone = onCompleted) }
    }
    composable<SettingsImportRoute> {
        ImportScreen(
            onBack = { navController.popBackStack() },
            onDone = { navController.popBackStack() },
            inOnboarding = false,
        )
    }
}
