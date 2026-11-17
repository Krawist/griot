package com.griot.core.ai

/**
 * [AiAssistant] for tests: answers with [location], [suggestions] and [remarks], or `null` without a [location];
 * [requests] records what was asked, in order.
 */
class FakeAiAssistant(
    private val location: AiLocation? = null,
    private val suggestions: List<CheckedSuggestion> = emptyList(),
    private val remarks: List<String> = emptyList(),
) : AiAssistant {
    val requests = mutableListOf<Pair<String, VerseRequest>>()

    override suspend fun location() = location

    override suspend fun rephrase(request: VerseRequest): AiAnswer<CheckedSuggestion>? {
        requests += "rephrase" to request
        return location?.let { AiAnswer(items = suggestions, location = it) }
    }

    override suspend fun critique(request: VerseRequest): AiAnswer<String>? {
        requests += "critique" to request
        return location?.let { AiAnswer(items = remarks, location = it) }
    }
}
