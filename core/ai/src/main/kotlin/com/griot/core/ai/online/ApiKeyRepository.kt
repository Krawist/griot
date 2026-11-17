package com.griot.core.ai.online

import kotlinx.coroutines.flow.Flow

/**
 * The key of the online model (an Anthropic API key), pasted by the author in the settings. It is kept on the phone
 * only, encrypted by a key of the Android Keystore, and never written in clear.
 */
interface ApiKeyRepository {
    /** Whether a key is saved. */
    val isSet: Flow<Boolean>

    /** The saved key; `null` when none is, or when the Keystore lost the key that encrypted it. */
    suspend fun read(): String?

    /** Checks [key] with Anthropic, without the spaces around it, and saves it once accepted. */
    suspend fun save(key: String): KeyCheck

    /** Forgets the saved key. */
    suspend fun clear()
}
