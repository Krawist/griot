package com.griot.core.ai

/**
 * What the AI panel shows for one request.
 *
 * @property items The suggestions or remarks kept after the checks, at most three.
 * @property location Where the model that answered runs.
 */
data class AiAnswer<T>(val items: List<T>, val location: AiLocation)
