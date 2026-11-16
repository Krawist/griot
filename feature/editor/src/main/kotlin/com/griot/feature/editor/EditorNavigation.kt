package com.griot.feature.editor

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.griot.core.design.component.PlaceholderAction
import com.griot.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object EditorGraph

@Serializable
internal data object EditorRoute

fun NavController.navigateToEditor() = navigate(EditorGraph)

/** Structured editor, verses, font size and paste splitting (GRI-14 to GRI-18). */
fun NavGraphBuilder.editorGraph(onBack: () -> Unit) {
    navigation<EditorGraph>(startDestination = EditorRoute) {
        composable<EditorRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.editor_title),
                subtitle = stringResource(R.string.editor_subtitle),
                actions = listOf(
                    PlaceholderAction(stringResource(R.string.editor_back), onBack),
                ),
            )
        }
    }
}
