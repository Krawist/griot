package com.griot.core.ai

import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.phonetics.PhoneticEngines
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** Default implementation for [AiAssistant]: the phone's model first, then the online one. */
@Singleton
internal class AiAssistantImpl @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards AiProvider>,
    private val engines: PhoneticEngines,
    private val preferences: UserPreferencesRepository,
) : AiAssistant {
    override suspend fun availability(): AiAvailability {
        val statuses = allowed().map { it to it.status() }
        return AiAvailability(
            location = statuses.firstOrNull { it.second == AiStatus.READY }?.first?.location,
            preparing = statuses.any { it.second == AiStatus.PREPARING },
        )
    }

    override suspend fun status(location: AiLocation): AiStatus =
        providers.firstOrNull { it.location == location }?.status() ?: AiStatus.UNAVAILABLE

    override suspend fun rephrase(request: VerseRequest): AiAnswer<CheckedSuggestion>? {
        val provider = provider() ?: return null
        val answer = provider.generate(rephrasePrompt(request))
        val suggestions = checkSuggestions(
            engine = engines.of(request.language),
            request = request,
            suggestions = answerLines(answer),
        )
        return AiAnswer(items = suggestions, location = provider.location)
    }

    override suspend fun critique(request: VerseRequest): AiAnswer<String>? {
        val provider = provider() ?: return null
        val engine = engines.of(request.language)
        val counts = (request.before + request.verse + request.after).map { engine.countSyllables(it).syllables }
        val answer = provider.generate(critiquePrompt(request = request, counts = counts))
        val remarks = keepRemarks(remarks = answerLines(answer), request = request)
        return AiAnswer(items = remarks, location = provider.location)
    }

    override suspend fun suggestTitle(request: SongRequest): String? {
        val provider = provider() ?: return null
        return checkTitle(answerLines(provider.generate(titlePrompt(request)), max = 1).firstOrNull())
    }

    override suspend fun classify(request: SongRequest): Classification? {
        val provider = provider() ?: return null
        return checkClassification(answer = provider.generate(classifyPrompt(request)), request = request)
    }

    // The phone's model when it can answer, else the online one.
    private suspend fun provider(): AiProvider? = allowed().firstOrNull { it.status() == AiStatus.READY }

    // The models the settings allow, the phone's first.
    private suspend fun allowed(): List<AiProvider> {
        val onDevice = preferences.onDeviceAi.first()
        return providers.filter { it.location != AiLocation.DEVICE || onDevice }.sortedBy { it.location.ordinal }
    }
}
