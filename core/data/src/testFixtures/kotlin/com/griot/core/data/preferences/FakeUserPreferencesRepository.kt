package com.griot.core.data.preferences

import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [UserPreferencesRepository] for tests. */
class FakeUserPreferencesRepository(onboardingCompleted: Boolean = false, authorName: String? = null) :
    UserPreferencesRepository {
    override val onboardingCompleted = MutableStateFlow(onboardingCompleted)

    override val authorName = MutableStateFlow(authorName)

    override suspend fun setOnboardingCompleted() {
        onboardingCompleted.value = true
    }

    override suspend fun setAuthorName(name: String) {
        authorName.value = name
    }
}
