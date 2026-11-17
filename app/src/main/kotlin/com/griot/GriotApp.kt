package com.griot

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.AnimatedPresence
import com.griot.core.design.component.BottomBarTab
import com.griot.core.design.component.BottomControls
import com.griot.core.design.component.GriotBottomBar
import com.griot.core.design.component.backdrop
import com.griot.core.design.component.screenBackdropShapes
import com.griot.core.design.glass.GlassScreen
import com.griot.core.design.theme.GriotMotion
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.SongLanguageSheet
import com.griot.feature.editor.editorGraph
import com.griot.feature.editor.navigateToEditor
import com.griot.feature.ideas.IdeasGraph
import com.griot.feature.ideas.ideasGraph
import com.griot.feature.library.LibraryGraph
import com.griot.feature.library.filing.FilingSheet
import com.griot.feature.library.libraryGraph
import com.griot.feature.library.navigateToTrash
import com.griot.feature.onboarding.OnboardingGraph
import com.griot.feature.onboarding.navigateToImport
import com.griot.feature.onboarding.onboardingGraph
import com.griot.feature.settings.SettingsGraph
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
    // The preferences load in a few milliseconds: the launch screen covers it, then fades into the first screen.
    val ready = uiState.takeIf { launchShown } as? MainUiState.Ready
    Crossfade(
        targetState = ready,
        animationSpec = GriotTheme.motion.tween(GriotMotion.LONG),
        label = "launch",
    ) { state ->
        if (state == null) {
            LaunchScreen()
        } else {
            GriotNavHost(
                onboardingCompleted = state.onboardingCompleted,
                onOnboardingCompleted = onOnboardingCompleted,
            )
        }
    }
}

// The tabs of the bottom bar, in order, by the route of their graph.
private val TabRoutes = listOf(LibraryGraph, IdeasGraph, SettingsGraph)

@Composable
private fun GriotNavHost(
    onboardingCompleted: Boolean,
    onOnboardingCompleted: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    // Chosen once: a new start destination would rebuild the graph under the back stack when the onboarding completes.
    val startDestination = remember { if (onboardingCompleted) LibraryGraph else OnboardingGraph }
    val motion = GriotTheme.motion
    // The library hides the bar while its songs scroll down.
    var barHidden by remember { mutableStateOf(false) }
    // The song the editor asked to file in « Dossier et genre », a sheet of the library.
    var filing by rememberSaveable { mutableStateOf<String?>(null) }
    // The song whose language a long press in the library asked for, a sheet of the editor.
    var language by rememberSaveable { mutableStateOf<String?>(null) }
    GlassScreen(
        // Drawn once behind every screen, which are transparent: glass cards and the bar blur these shapes.
        background = { Box(Modifier.fillMaxSize().backdrop(screenBackdropShapes())) },
        overlay = { TabsOverlay(navController = navController, hidden = barHidden) },
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { if (betweenTabs()) motion.tabEnter() else motion.screenEnter() },
            exitTransition = { if (betweenTabs()) motion.tabExit() else motion.screenExit() },
            popEnterTransition = { if (betweenTabs()) motion.tabEnter() else motion.screenPopEnter() },
            popExitTransition = { if (betweenTabs()) motion.tabExit() else motion.screenPopExit() },
        ) {
            onboardingGraph(
                navController = navController,
                onCompleted = {
                    onOnboardingCompleted()
                    navController.navigate(LibraryGraph) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                // The restore from Google Drive comes with GRI-30.
                onRestore = null,
            )
            libraryGraph(
                navController = navController,
                onOpenSong = { songId -> navController.navigateToEditor(songId) },
                onBack = { navController.popBackStack() },
                onSongLanguage = { songId -> language = songId },
                onControlsShownChange = { shown -> barHidden = !shown },
            )
            ideasGraph(onOpenSong = { songId -> navController.navigateToEditor(songId) })
            settingsGraph(
                onOpenImport = { navController.navigateToImport() },
                onOpenTrash = { navController.navigateToTrash() },
            )
            editorGraph(
                navController = navController,
                onBack = { navController.popBackStack() },
                onFile = { songId -> filing = songId },
            )
        }
    }
    filing?.let { songId -> FilingSheet(songIds = listOf(songId), onDismiss = { filing = null }) }
    language?.let { songId -> SongLanguageSheet(songId = songId, onDismiss = { language = null }) }
}

// The bottom bar, only on the tabs: it goes down during the onboarding, in the editor, and while [hidden] by a tab.
@Composable
private fun BoxScope.TabsOverlay(navController: NavHostController, hidden: Boolean) {
    val destination = navController.currentBackStackEntryAsState().value?.destination
    val selectedTab = TabRoutes.indexOfFirst { route ->
        destination?.hierarchy?.any { it.hasRoute(route::class) } == true
    }
    val motion = GriotTheme.motion
    AnimatedPresence(
        value = selectedTab.takeIf { it >= 0 },
        enter = motion.barEnter(),
        exit = motion.barExit(),
        modifier = Modifier.align(Alignment.BottomCenter),
    ) { tab ->
        BottomControls(shown = !hidden) {
            GriotBottomBar(
                tabs = listOf(
                    BottomBarTab(icon = DesignR.drawable.ic_nav_songs, label = stringResource(R.string.tab_songs)),
                    BottomBarTab(icon = DesignR.drawable.ic_nav_ideas, label = stringResource(R.string.tab_ideas)),
                    BottomBarTab(
                        icon = DesignR.drawable.ic_nav_settings,
                        label = stringResource(R.string.tab_settings),
                    ),
                ),
                selectedTab = tab,
                onTabSelected = { index -> navController.navigateToTab(TabRoutes[index]) },
            )
        }
    }
}

// Whether the navigation goes from a tab to another: tabs fade, side by side, instead of sliding.
private fun AnimatedContentTransitionScope<NavBackStackEntry>.betweenTabs() =
    initialState.destination.isTab() && targetState.destination.isTab()

private fun NavDestination.isTab() = TabRoutes.any { route -> hierarchy.any { it.hasRoute(route::class) } }

// « Chansons » stays at the bottom of the back stack: back from another tab returns to it, then leaves the app. Each
// tab keeps its own state when the user switches between them. It pops up to the library's screen, not to its graph
// entry: popping to the graph would remove the screen and leave nothing to go back to.
private fun NavHostController.navigateToTab(route: Any) {
    val libraryScreen = (graph.findNode<LibraryGraph>() as NavGraph).findStartDestination()
    navigate(route) {
        popUpTo(libraryScreen.id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
