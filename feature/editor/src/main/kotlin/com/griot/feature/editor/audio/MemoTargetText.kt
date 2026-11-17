package com.griot.feature.editor.audio

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.feature.editor.R
import com.griot.feature.editor.text

/** « Couplet 1 », « Couplet 1 · vers 2 » or « Toute la chanson ». */
@Composable
internal fun MemoTarget.text(): String {
    val section = label?.text() ?: return stringResource(R.string.recording_whole_song)
    return verseNumber?.let { stringResource(R.string.editor_verse_place, section, it) } ?: section
}
