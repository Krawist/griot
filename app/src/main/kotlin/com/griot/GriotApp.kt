package com.griot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

/** Root of the app: the onboarding on first launch, then the library. */
@Composable
internal fun GriotApp(uiState: MainUiState, onOnboardingCompleted: () -> Unit) {
    when (uiState) {
        // Plain background while the preferences load (a few milliseconds).
        MainUiState.Loading -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        is MainUiState.Ready -> GriotNavHost(
            onboardingCompleted = uiState.onboardingCompleted,
            onOnboardingCompleted = onOnboardingCompleted,
        )
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
