package com.griot.core.data.tidy

import com.griot.core.data.model.TidyUpCandidates
import com.griot.core.data.model.TidyUpKind
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [TidyUpRepository] for tests: [due] and [candidates] are what it gives; [calls] records every change
 * asked, such as `"refuse TITLE my-song-id"`, in order.
 */
class FakeTidyUpRepository(due: Boolean = false, candidates: TidyUpCandidates = TidyUpCandidates()) :
    TidyUpRepository {
    val due = MutableStateFlow(due)

    val candidates = MutableStateFlow(candidates)

    val calls = mutableListOf<String>()

    override fun observeDue() = due

    override fun observeCandidates() = candidates

    override suspend fun refuse(kind: TidyUpKind, keys: List<String>) {
        calls += "refuse $kind ${keys.joinToString()}"
    }

    override suspend fun mergeDuplicates(keepId: String, duplicates: List<String>) {
        calls += "merge $keepId ${duplicates.joinToString()}"
    }

    override suspend fun markReviewed() {
        calls += "reviewed"
    }
}
