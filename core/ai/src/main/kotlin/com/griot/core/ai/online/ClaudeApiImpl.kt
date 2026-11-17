package com.griot.core.ai.online

import android.util.Log
import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.errors.AnthropicException
import com.anthropic.errors.AnthropicServiceException
import com.anthropic.errors.PermissionDeniedException
import com.anthropic.errors.UnauthorizedException
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.OutputConfig
import com.anthropic.models.messages.StopReason
import com.griot.core.ai.AiPrompt
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Default implementation for [ClaudeApi]: the Anthropic SDK and Claude Haiku 5.5, the small low-cost model of the
 * ticket, at a low effort so that it answers in a few seconds. One client per key.
 */
@Singleton
internal class ClaudeApiImpl @Inject constructor() : ClaudeApi {
    private var client: Pair<String, AnthropicClient>? = null

    override suspend fun check(key: String): KeyCheck = withContext(Dispatchers.IO) {
        try {
            clientFor(key).models().retrieve(MODEL)
            KeyCheck.ACCEPTED
        } catch (e: UnauthorizedException) {
            Log.w(TAG, "Anthropic refuses the key: ${e.statusCode()}")
            KeyCheck.REFUSED
        } catch (e: PermissionDeniedException) {
            Log.w(TAG, "Anthropic refuses the key: ${e.statusCode()}")
            KeyCheck.REFUSED
        } catch (e: AnthropicServiceException) {
            Log.w(TAG, "Anthropic answers ${e.statusCode()}")
            KeyCheck.UNREACHABLE
        } catch (e: AnthropicException) {
            // No network, a timeout.
            Log.w(TAG, "Anthropic cannot be reached: ${e.javaClass.simpleName}")
            KeyCheck.UNREACHABLE
        }
    }

    override suspend fun generate(key: String, prompt: AiPrompt): String = withContext(Dispatchers.IO) {
        val params = MessageCreateParams.builder()
            .model(MODEL)
            .maxTokens(MAX_TOKENS)
            .system(prompt.instructions)
            .addUserMessage(prompt.request)
            .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.LOW).build())
            .build()
        val message = clientFor(key).messages().create(params)
        if (message.stopReason().getOrNull() == StopReason.REFUSAL) {
            Log.w(TAG, "The model declined to answer")
            return@withContext ""
        }
        message.content().mapNotNull { it.text().getOrNull()?.text() }.joinToString(separator = "\n")
    }

    @Synchronized
    private fun clientFor(key: String): AnthropicClient {
        client?.let { (clientKey, it) -> if (clientKey == key) return it }
        client?.second?.close()
        return AnthropicOkHttpClient.builder()
            .apiKey(key)
            .timeout(TIMEOUT)
            .maxRetries(1)
            .build()
            .also { client = key to it }
    }

    private companion object {
        const val TAG = "ClaudeApi"
        const val MODEL = "claude-haiku-5-5"

        // Room for the model's thinking and three verses or remarks.
        const val MAX_TOKENS = 4_000L

        // A few seconds, then the panel offers to try again.
        val TIMEOUT: Duration = Duration.ofSeconds(15)
    }
}
