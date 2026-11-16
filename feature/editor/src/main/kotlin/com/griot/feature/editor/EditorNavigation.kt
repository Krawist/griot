package com.griot.feature.editor

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.griot.core.design.component.PlaceholderAction
import com.griot.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

/**
 * The editor, opened on one song.
 *
 * @property songId UUID of the song.
 */
@Serializable
data class EditorRoute(val songId: String)

fun NavController.navigateToEditor(songId: String) = navigate(EditorRoute(songId))

/** Structured editor, verses, font size and paste splitting (GRI-14 to GRI-18). */
fun NavGraphBuilder.editorGraph(onBack: () -> Unit) {
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
