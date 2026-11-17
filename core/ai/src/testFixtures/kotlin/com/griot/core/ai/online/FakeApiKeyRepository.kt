package com.griot.core.ai.online

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [ApiKeyRepository] for tests: Anthropic answers [check], and an accepted key is kept as given. */
class FakeApiKeyRepository(key: String? = null, private val check: KeyCheck = KeyCheck.ACCEPTED) : ApiKeyRepository {
    val key = MutableStateFlow(key)

    override val isSet = this.key.map { it != null }

    override suspend fun read() = key.value

    override suspend fun save(key: String): KeyCheck {
        if (check == KeyCheck.ACCEPTED) this.key.value = key
        return check
    }

    override suspend fun clear() {
        key.value = null
    }
}
