package com.griot.feature.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingGraph

@Serializable
internal data object WelcomeRoute

/**
 * Welcome, name, language, sync and import (GRI-07 to GRI-11).
 *
 * @param onRestore Opens the restore from Google Drive, which skips the rest of the onboarding; `null` until GRI-30.
 */
fun NavGraphBuilder.onboardingGraph(onCompleted: () -> Unit, onRestore: (() -> Unit)?) {
    navigation<OnboardingGraph>(startDestination = WelcomeRoute) {
        composable<WelcomeRoute> { WelcomeScreen(onStart = onCompleted, onRestore = onRestore) }
    }
}
