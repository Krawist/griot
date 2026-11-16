package com.griot.feature.library.trash

/**
 * What the trash shows.
 *
 * @property items Songs in the trash, last deleted first.
 * @property loading Whether the songs are still being read, so that no empty state flashes at start.
 */
internal data class TrashUiState(val items: List<TrashItem> = emptyList(), val loading: Boolean = true)
