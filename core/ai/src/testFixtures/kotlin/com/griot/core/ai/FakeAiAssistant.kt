package com.griot.core.ai

/**
 * [AiAssistant] for tests: answers with [location], [suggestions], [remarks], [title] and [classification], or `null`
 * without a [location]; the
 * model of the phone is [deviceStatus], downloading when [preparing]; [requests] records what was asked, in order.
 */
class FakeAiAssistant(
    private val location: AiLocation? = null,
    private val preparing: Boolean = false,
    private val deviceStatus: AiStatus = AiStatus.UNAVAILABLE,
    private val suggestions: List<CheckedSuggestion> = emptyList(),
    private val remarks: List<String> = emptyList(),
    private val title: String? = null,
    private val classification: Classification? = null,
) : AiAssistant {
    val requests = mutableListOf<Pair<String, VerseRequest>>()

    override suspend fun availability() = AiAvailability(location = location, preparing = preparing)

    override suspend fun status(location: AiLocation) =
        if (location == AiLocation.DEVICE) deviceStatus else AiStatus.UNAVAILABLE

    override suspend fun rephrase(request: VerseRequest): AiAnswer<CheckedSuggestion>? {
        requests += "rephrase" to request
        return location?.let { AiAnswer(items = suggestions, location = it) }
    }

    override suspend fun critique(request: VerseRequest): AiAnswer<String>? {
        requests += "critique" to request
        return location?.let { AiAnswer(items = remarks, location = it) }
    }

    override suspend fun suggestTitle(request: SongRequest): String? = title.takeIf { location != null }

    override suspend fun classify(request: SongRequest): Classification? = classification.takeIf { location != null }
}
