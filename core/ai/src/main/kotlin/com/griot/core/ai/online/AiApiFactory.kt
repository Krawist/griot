package com.griot.core.ai.online

/** Gives the online API that a key opens. */
internal interface AiApiFactory {
    /** The API of the provider whose keys look like [key]; `null` when no provider Griot knows has such keys. */
    fun create(key: String): AiApi?
}
