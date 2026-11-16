package com.griot.feature.editor.history

import com.griot.core.data.song.SectionLabel

/**
 * What the history shows.
 *
 * @property loading Whether the versions are still being read.
 * @property scope One verse or the whole song.
 * @property title The song's title; empty for a song not named.
 * @property verseSection Label of the section of the verse, when the history was opened on a verse.
 * @property verseNumber Rank of the verse in its section, from 1, when the history was opened on a verse.
 * @property entries The text as it is now, then the versions, newest first.
 */
internal data class HistoryUiState(
    val loading: Boolean = true,
    val scope: HistoryScope = HistoryScope.SONG,
    val title: String = "",
    val verseSection: SectionLabel? = null,
    val verseNumber: Int? = null,
    val entries: List<HistoryEntry> = emptyList(),
)
