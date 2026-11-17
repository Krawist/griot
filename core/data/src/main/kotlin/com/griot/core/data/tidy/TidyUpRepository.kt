package com.griot.core.data.tidy

import com.griot.core.data.model.TidyUpCandidates
import com.griot.core.data.model.TidyUpKind
import kotlinx.coroutines.flow.Flow

/** The tidy-up (GRI-59): when to propose it, what it could change, what the author refused, and merging duplicates. */
interface TidyUpRepository {
    /** Whether a tidy-up is due: after an import, or a week after the author last looked at the proposals. */
    fun observeDue(): Flow<Boolean>

    /** The songs a tidy-up could change, without those the author refused. */
    fun observeCandidates(): Flow<TidyUpCandidates>

    /** The author refused the proposals of [kind] about each of [keys]: they are never proposed again. */
    suspend fun refuse(kind: TidyUpKind, keys: List<String>)

    /**
     * Merges the [duplicates] into the song [keepId]: their versions become its own, their folders are added to it,
     * and they go to the trash. Nothing is deleted for good.
     */
    suspend fun mergeDuplicates(keepId: String, duplicates: List<String>)

    /** The author looked at the proposals: the next ones come after an import, or a week later. */
    suspend fun markReviewed()

    companion object {
        /** The key of a group of duplicates: their ids, sorted. */
        fun duplicateKey(songIds: List<String>): String = songIds.sorted().joinToString(separator = ",")
    }
}
