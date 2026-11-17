package com.griot.feature.editor.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Records a memo for the song [songId] when [request] is set: the microphone first, then the recording sheet.
 * [onClosed] when it is kept, thrown away or the microphone refused.
 */
@Composable
internal fun MemoRecorder(songId: String, request: MemoRequest?, onClosed: () -> Unit) {
    var granted by rememberSaveable(request) { mutableStateOf(false) }
    MicrophoneAccess(wanted = request != null && !granted, onGranted = { granted = true }, onDismiss = onClosed)
    if (request != null && granted) {
        RecordingSheet(songId = songId, verseId = request.verseId, onVerse = request.onVerse, onClosed = onClosed)
    }
}
