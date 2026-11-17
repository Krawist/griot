package com.griot.feature.library.tidy

import com.griot.core.data.model.TidyUpCandidates

/** The AI is asked about this many songs of each kind per tidy-up: each answer takes a few seconds. */
internal const val AI_SONGS = 8

/**
 * How many songs a tidy-up would change: the duplicates and the old drafts, and, when a model can answer
 * ([withAi]), the first songs without a title or a folder.
 */
internal fun TidyUpCandidates.songCount(withAi: Boolean): Int {
    val byCode = duplicates.flatten().map { it.id } + staleDrafts.map { it.id }
    val byAi = if (withAi) {
        untitled.take(AI_SONGS).map {
            it.id
        } + unclassified.take(AI_SONGS).map { it.id }
    } else {
        emptyList()
    }
    return (byCode + byAi).distinct().size
}
