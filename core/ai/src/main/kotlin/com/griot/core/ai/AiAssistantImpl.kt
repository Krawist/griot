package com.griot.core.ai

import com.griot.core.data.model.PreferredAi
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.phonetics.PhoneticEngines
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Default implementation for [AiAssistant]: the model the author prefers first, then the other one, when it cannot
 * answer or fails.
 */
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
        val (answer, location) = generate(rephrasePrompt(request)) ?: return null
        val suggestions = checkSuggestions(
            engine = engines.of(request.language),
            request = request,
            suggestions = answerLines(answer),
        )
        return AiAnswer(items = suggestions, location = location)
    }

    override suspend fun critique(request: VerseRequest): AiAnswer<String>? {
        val engine = engines.of(request.language)
        val counts = (request.before + request.verse + request.after).map { engine.countSyllables(it).syllables }
        val (answer, location) = generate(critiquePrompt(request = request, counts = counts)) ?: return null
        val remarks = keepRemarks(remarks = answerLines(answer), request = request)
        return AiAnswer(items = remarks, location = location)
    }

    override suspend fun suggestTitle(request: SongRequest): String? {
        val (answer) = generate(titlePrompt(request)) ?: return null
        return checkTitle(answerLines(answer, max = 1).firstOrNull())
    }

    override suspend fun classify(request: SongRequest): Classification? {
        val (answer) = generate(classifyPrompt(request)) ?: return null
        return checkClassification(answer = answer, request = request)
    }

    // The answer to [prompt] and where it comes from: the allowed models that can answer are asked in turn, the
    // preferred first, a failure passing to the next one; `null` when none can answer, the last failure when all fail.
    @Suppress("TooGenericExceptionCaught") // Any failure of a model, on the phone or online.
    private suspend fun generate(prompt: AiPrompt): Pair<String, AiLocation>? {
        var failure: Exception? = null
        for (provider in allowed()) {
            if (provider.status() != AiStatus.READY) continue
            try {
                return provider.generate(prompt) to provider.location
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failure = e
            }
        }
        failure?.let { throw it }
        return null
    }

    // The models the settings allow, the preferred first.
    private suspend fun allowed(): List<AiProvider> {
        val onDevice = preferences.onDeviceAi.first()
        val online = preferences.onlineAi.first()
        val preferred = when (preferences.preferredAi.first()) {
            PreferredAi.DEVICE -> AiLocation.DEVICE
            PreferredAi.ONLINE -> AiLocation.ONLINE
        }
        return providers
            .filter {
                when (it.location) {
                    AiLocation.DEVICE -> onDevice
                    AiLocation.ONLINE -> online
                }
            }
            .sortedBy { if (it.location == preferred) 0 else 1 }
    }
}
