package com.griot.core.data.model

/**
 * The songs a tidy-up could change, without those the author refused already.
 *
 * @property duplicates Groups of songs with the same text, the last changed first in each: the one to keep.
 * @property staleDrafts The drafts untouched for more than 30 days and not in « Archives ».
 * @property untitled The songs without a title but with text, for the AI to name.
 * @property unclassified The songs in no folder and with no genre, for the AI to classify.
 */
data class TidyUpCandidates(
    val duplicates: List<List<LibrarySong>> = emptyList(),
    val staleDrafts: List<LibrarySong> = emptyList(),
    val untitled: List<LibrarySong> = emptyList(),
    val unclassified: List<LibrarySong> = emptyList(),
)
