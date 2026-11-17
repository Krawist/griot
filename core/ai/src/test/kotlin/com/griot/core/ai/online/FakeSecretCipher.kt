package com.griot.core.ai.online

/** [SecretCipher] for tests: the secret behind a visible prefix, so that tests see it was encrypted. */
internal class FakeSecretCipher : SecretCipher {
    override suspend fun encrypt(secret: String) = "encrypted:$secret"

    override suspend fun decrypt(encrypted: String) = encrypted.removePrefix("encrypted:")
}
