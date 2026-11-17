package com.griot.feature.editor

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.griot.feature.editor.audio.AudiosScreen
import com.griot.feature.editor.history.HistoryScreen
import kotlinx.serialization.Serializable

/**
 * The editor, opened on one song.
 *
 * @property songId UUID of the song.
 */
@Serializable
data class EditorRoute(val songId: String)

fun NavController.navigateToEditor(songId: String) = navigate(EditorRoute(songId))

/**
 * The versions of a song.
 *
 * @property songId UUID of the song.
 * @property verseId UUID of the verse it opens on; `null` for the whole song.
 */
@Serializable
internal data class HistoryRoute(val songId: String, val verseId: String?)

/**
 * The audios of a song (GRI-51).
 *
 * @property songId UUID of the song.
 */
@Serializable
internal data class AudiosRoute(val songId: String)

/**
 * Structured editor, verses, font size and paste splitting (GRI-14 to GRI-18).
 *
 * @param onFile Opens « Dossier et genre » for the song with this id.
 */
fun NavGraphBuilder.editorGraph(navController: NavController, onBack: () -> Unit, onFile: (String) -> Unit) {
    composable<EditorRoute> { entry ->
        val songId = entry.toRoute<EditorRoute>().songId
        EditorScreen(
            songId = songId,
            onBack = onBack,
            onOpenHistory = { verseId -> navController.navigate(HistoryRoute(songId = songId, verseId = verseId)) },
            onFile = { onFile(songId) },
            onOpenAudios = { navController.navigate(AudiosRoute(songId)) },
        )
    }
    composable<AudiosRoute> { entry ->
        AudiosScreen(songId = entry.toRoute<AudiosRoute>().songId, onBack = { navController.popBackStack() })
    }
    composable<HistoryRoute> { entry ->
        val route = entry.toRoute<HistoryRoute>()
        HistoryScreen(songId = route.songId, verseId = route.verseId, onBack = { navController.popBackStack() })
    }
}
