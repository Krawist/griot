package com.griot.core.ai.online

/** Encrypts a secret with a key that never leaves the phone. */
internal interface SecretCipher {
    /** [secret] encrypted, as text to store. */
    suspend fun encrypt(secret: String): String

    /** The secret [encrypted] by [encrypt]; `null` when it cannot be read back, its key being gone. */
    suspend fun decrypt(encrypted: String): String?
}
