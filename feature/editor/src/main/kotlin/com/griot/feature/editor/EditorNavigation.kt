package com.griot.feature.editor

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
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
    composable<EditorRoute> { entry -> EditorScreen(songId = entry.toRoute<EditorRoute>().songId, onBack = onBack) }
}
