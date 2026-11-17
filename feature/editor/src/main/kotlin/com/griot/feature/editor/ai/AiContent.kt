package com.griot.feature.editor.ai

/**
 * What a model tab of the AI panel shows.
 *
 * @property loading Whether the model is answering.
 * @property items The suggestions or remarks of the last answer.
 * @property answered Whether the model answered once: an empty answer then says that nothing was kept.
 * @property unavailable Whether no model is available on the phone nor online.
 * @property failed Whether the last request failed: the tab offers to retry.
 */
internal data class AiContent<T>(
    val loading: Boolean = false,
    val items: List<T> = emptyList(),
    val answered: Boolean = false,
    val unavailable: Boolean = false,
    val failed: Boolean = false,
)
