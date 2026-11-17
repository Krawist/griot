package com.griot.feature.library.tidy

/**
 * What the proposals screen shows.
 *
 * @property proposals Every proposal, by kind: classify, titles, duplicates, archive.
 * @property thinking Whether the AI is still working on proposals.
 * @property loading Whether the songs are still being read.
 */
internal data class TidyUpUiState(
    val proposals: List<TidyUpProposal> = emptyList(),
    val thinking: Boolean = false,
    val loading: Boolean = true,
)
