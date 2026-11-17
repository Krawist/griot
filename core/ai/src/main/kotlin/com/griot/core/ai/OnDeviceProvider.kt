package com.griot.core.ai

import android.util.Log
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Feature
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.SystemInstruction
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Gemini Nano on the phone, through the ML Kit GenAI Prompt API and the phone's AICore service: the text never leaves
 * the phone. A phone that can have the model but has not downloaded it yet downloads it on the first check.
 */
@Singleton
internal class OnDeviceProvider @Inject constructor() : AiProvider {
    override val location = AiLocation.DEVICE

    private val model: GenerativeModel by lazy { Generation.getClient() }

    // The download outlives the screen that asked for it.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var download: Job? = null

    override suspend fun status(): AiStatus = try {
        when (model.checkStatus()) {
            FeatureStatus.AVAILABLE -> AiStatus.READY
            FeatureStatus.DOWNLOADABLE -> AiStatus.PREPARING.also { startDownload() }
            FeatureStatus.DOWNLOADING -> AiStatus.PREPARING
            else -> AiStatus.UNAVAILABLE
        }
    } catch (e: GenAiException) {
        // AICore missing, too old or incompatible: this phone has no model.
        Log.w(TAG, "Gemini Nano is not available", e)
        AiStatus.UNAVAILABLE
    }

    override suspend fun generate(prompt: AiPrompt): String {
        val text = TextPart(prompt.request)
        // A new seed for each request (never negative): « Autres idées » asks the same prompt again for other answers.
        val sampling: GenerateContentRequest.Builder.() -> Unit = {
            temperature = TEMPERATURE
            seed = Random.nextInt(from = 0, until = Int.MAX_VALUE)
        }
        val request = if (Feature.SYSTEM_PROMPT in model.getAvailableFeatures()) {
            generateContentRequest(SystemInstruction(prompt.instructions), text, sampling)
        } else {
            generateContentRequest(TextPart("${prompt.instructions}\n\n${prompt.request}"), sampling)
        }
        return model.generateContent(request).candidates.firstOrNull()?.text.orEmpty()
    }

    private fun startDownload() {
        if (download?.isActive == true) return
        download = scope.launch {
            model.download()
                .catch { Log.w(TAG, "The download of Gemini Nano failed", it) }
                .collect { Log.d(TAG, "Gemini Nano download: $it") }
        }
    }

    private companion object {
        const val TAG = "OnDeviceAi"

        // Varied enough for new ideas, low enough to keep the verse's meaning.
        const val TEMPERATURE = 0.8f
    }
}
