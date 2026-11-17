package com.griot.core.ai.online

import com.griot.core.data.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Default implementation for [ApiKeyRepository]: the encrypted key in the preferences. */
@Singleton
internal class ApiKeyRepositoryImpl @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val cipher: SecretCipher,
    private val apis: AiApiFactory,
) : ApiKeyRepository {
    override val isSet: Flow<Boolean> = preferences.onlineAiKey.map { it != null }

    override suspend fun read(): String? = preferences.onlineAiKey.first()?.let { cipher.decrypt(it) }

    override suspend fun save(key: String): KeyCheck {
        val trimmed = key.trim()
        // A key no provider has is refused without being sent anywhere.
        val check = apis.create(trimmed)?.check() ?: KeyCheck.REFUSED
        if (check == KeyCheck.ACCEPTED) preferences.setOnlineAiKey(cipher.encrypt(trimmed))
        return check
    }

    override suspend fun clear() {
        preferences.setOnlineAiKey(null)
    }
}
