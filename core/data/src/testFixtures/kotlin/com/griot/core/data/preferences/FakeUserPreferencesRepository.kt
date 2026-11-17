package com.griot.core.data.preferences

import com.griot.core.data.model.SongLanguage
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [UserPreferencesRepository] for tests. */
class FakeUserPreferencesRepository(
    onboardingCompleted: Boolean = false,
    authorName: String? = null,
    defaultSongLanguage: SongLanguage = SongLanguage.AUTOMATIC,
    editorFontSize: Int = UserPreferencesRepository.DEFAULT_EDITOR_FONT_SIZE,
    showRhymes: Boolean = true,
    onDeviceAi: Boolean = true,
    lastSyncedAt: Instant? = null,
    syncCursor: String? = null,
) : UserPreferencesRepository {
    override val onboardingCompleted = MutableStateFlow(onboardingCompleted)

    override val authorName = MutableStateFlow(authorName)

    override val defaultSongLanguage = MutableStateFlow(defaultSongLanguage)

    override val editorFontSize = MutableStateFlow(editorFontSize)

    override val showRhymes = MutableStateFlow(showRhymes)

    override val onDeviceAi = MutableStateFlow(onDeviceAi)

    override val lastSyncedAt = MutableStateFlow(lastSyncedAt)

    override val syncCursor = MutableStateFlow(syncCursor)

    override suspend fun setOnboardingCompleted() {
        onboardingCompleted.value = true
    }

    override suspend fun setAuthorName(name: String) {
        authorName.value = name
    }

    override suspend fun setDefaultSongLanguage(language: SongLanguage) {
        defaultSongLanguage.value = language
    }

    override suspend fun setEditorFontSize(size: Int) {
        editorFontSize.value = size
    }

    override suspend fun setShowRhymes(show: Boolean) {
        showRhymes.value = show
    }

    override suspend fun setOnDeviceAi(allowed: Boolean) {
        onDeviceAi.value = allowed
    }

    override suspend fun setSynced(at: Instant, cursor: String?) {
        lastSyncedAt.value = at
        syncCursor.value = cursor
    }
}
