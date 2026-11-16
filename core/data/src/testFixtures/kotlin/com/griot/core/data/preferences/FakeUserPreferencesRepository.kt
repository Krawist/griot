package com.griot.core.data.preferences

import com.griot.core.data.model.SongLanguage
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [UserPreferencesRepository] for tests. */
class FakeUserPreferencesRepository(
    onboardingCompleted: Boolean = false,
    authorName: String? = null,
    defaultSongLanguage: SongLanguage = SongLanguage.AUTOMATIC,
) : UserPreferencesRepository {
    override val onboardingCompleted = MutableStateFlow(onboardingCompleted)

    override val authorName = MutableStateFlow(authorName)

    override val defaultSongLanguage = MutableStateFlow(defaultSongLanguage)

    override suspend fun setOnboardingCompleted() {
        onboardingCompleted.value = true
    }

    override suspend fun setAuthorName(name: String) {
        authorName.value = name
    }

    override suspend fun setDefaultSongLanguage(language: SongLanguage) {
        defaultSongLanguage.value = language
    }
}
