package com.griot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.griot.feature.editor.editorGraph
import com.griot.feature.editor.navigateToEditor
import com.griot.feature.ideas.ideasGraph
import com.griot.feature.ideas.navigateToIdeas
import com.griot.feature.library.LibraryGraph
import com.griot.feature.library.libraryGraph
import com.griot.feature.onboarding.OnboardingGraph
import com.griot.feature.onboarding.onboardingGraph
import com.griot.feature.settings.navigateToSettings
import com.griot.feature.settings.settingsGraph
import kotlinx.coroutines.delay

/** Root of the app: the launch screen, then the onboarding on first launch or the library. */
@Composable
internal fun GriotApp(uiState: MainUiState, onOnboardingCompleted: () -> Unit) {
    // Saved so that a configuration change (rotation, dark mode) does not show the launch screen again.
    var launchShown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(LaunchDuration)
        launchShown = true
    }
    // The preferences load in a few milliseconds: the launch screen covers it.
    if (launchShown && uiState is MainUiState.Ready) {
        GriotNavHost(
            onboardingCompleted = uiState.onboardingCompleted,
            onOnboardingCompleted = onOnboardingCompleted,
        )
    } else {
        LaunchScreen()
    }
}

@Composable
private fun GriotNavHost(
    onboardingCompleted: Boolean,
    onOnboardingCompleted: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = if (onboardingCompleted) LibraryGraph else OnboardingGraph,
    ) {
        onboardingGraph(
            onCompleted = {
                onOnboardingCompleted()
                navController.navigate(LibraryGraph) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            },
        )
        libraryGraph(
            onNewSong = { navController.navigateToEditor() },
            onOpenIdeas = { navController.navigateToIdeas() },
            onOpenSettings = { navController.navigateToSettings() },
        )
        editorGraph(onBack = { navController.popBackStack() })
        ideasGraph(onBack = { navController.popBackStack() })
        settingsGraph(onBack = { navController.popBackStack() })
    }
}
