package com.griot.feature.onboarding

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.griot.core.design.component.PlaceholderAction
import com.griot.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingGraph

@Serializable
internal data object WelcomeRoute

/** Welcome, name, language, sync and import (GRI-07 to GRI-11). */
fun NavGraphBuilder.onboardingGraph(onCompleted: () -> Unit) {
    navigation<OnboardingGraph>(startDestination = WelcomeRoute) {
        composable<WelcomeRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.onboarding_title),
                subtitle = stringResource(R.string.onboarding_subtitle),
                actions = listOf(
                    PlaceholderAction(stringResource(R.string.onboarding_start), onCompleted),
                ),
            )
        }
    }
}
