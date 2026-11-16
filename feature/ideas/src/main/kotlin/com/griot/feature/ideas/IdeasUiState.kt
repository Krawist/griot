package com.griot.feature.ideas

import com.griot.core.data.model.Idea
import com.griot.core.data.model.LibrarySong

/**
 * What the ideas box shows.
 *
 * @property loading Whether the ideas are still being read.
 * @property items The ideas, newest first.
 * @property songs The songs an idea can be linked to, last changed first.
 * @property deleted The idea just deleted, while the undo bar shows; `null` otherwise.
 */
internal data class IdeasUiState(
    val loading: Boolean = true,
    val items: List<IdeaItem> = emptyList(),
    val songs: List<LibrarySong> = emptyList(),
    val deleted: Idea? = null,
)
