package com.griot.core.ai

import com.griot.core.phonetics.PhoneticEngines
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [AiAssistant]: the phone's model first, then the online one. */
@Singleton
internal class AiAssistantImpl @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards AiProvider>,
    private val engines: PhoneticEngines,
) : AiAssistant {
    override suspend fun location(): AiLocation? = provider()?.location

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
        return AiAnswer(items = answerLines(answer), location = provider.location)
    }

    // The phone's model when it can answer, else the online one.
    private suspend fun provider(): AiProvider? =
        providers.sortedBy { it.location.ordinal }.firstOrNull { it.isAvailable() }
}
