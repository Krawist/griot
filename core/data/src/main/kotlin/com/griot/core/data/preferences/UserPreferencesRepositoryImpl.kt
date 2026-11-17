package com.griot.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.griot.core.data.model.SongLanguage
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default implementation for [UserPreferencesRepository], on DataStore. */
@Singleton
internal class UserPreferencesRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) :
    UserPreferencesRepository {
    override val onboardingCompleted: Flow<Boolean> = dataStore.data.map { it[OnboardingCompleted] ?: false }

    override val authorName: Flow<String?> = dataStore.data.map { it[AuthorName] }

    // Stored by name; until the onboarding saves one (or for « AUTOMATIC », stored before GRI-54), the phone's.
    override val defaultSongLanguage: Flow<SongLanguage> = dataStore.data.map { preferences ->
        SongLanguage.entries.firstOrNull { it.name == preferences[DefaultSongLanguage] } ?: phoneLanguage()
    }

    private fun phoneLanguage() =
        if (Locale.getDefault().language == Locale.ENGLISH.language) SongLanguage.ENGLISH else SongLanguage.FRENCH

    override val editorFontSize: Flow<Int> =
        dataStore.data.map { it[EditorFontSize] ?: UserPreferencesRepository.DEFAULT_EDITOR_FONT_SIZE }

    override val showRhymes: Flow<Boolean> = dataStore.data.map { it[ShowRhymes] ?: true }

    override val onDeviceAi: Flow<Boolean> = dataStore.data.map { it[OnDeviceAi] ?: true }

    override val lastSyncedAt: Flow<Instant?> = dataStore.data.map { preferences ->
        preferences[LastSyncedAt]?.let(Instant::ofEpochMilli)
    }

    override val syncCursor: Flow<String?> = dataStore.data.map { it[SyncCursor] }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { it[OnboardingCompleted] = true }
    }

    override suspend fun setAuthorName(name: String) {
        dataStore.edit { it[AuthorName] = name }
    }

    override suspend fun setDefaultSongLanguage(language: SongLanguage) {
        dataStore.edit { it[DefaultSongLanguage] = language.name }
    }

    override suspend fun setEditorFontSize(size: Int) {
        dataStore.edit { it[EditorFontSize] = size }
    }

    override suspend fun setShowRhymes(show: Boolean) {
        dataStore.edit { it[ShowRhymes] = show }
    }

    override suspend fun setOnDeviceAi(allowed: Boolean) {
        dataStore.edit { it[OnDeviceAi] = allowed }
    }

    override suspend fun setSynced(at: Instant, cursor: String?) {
        dataStore.edit { preferences ->
            preferences[LastSyncedAt] = at.toEpochMilli()
            if (cursor != null) preferences[SyncCursor] = cursor else preferences.remove(SyncCursor)
        }
    }

    private companion object {
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val AuthorName = stringPreferencesKey("author_name")
        val DefaultSongLanguage = stringPreferencesKey("default_song_language")
        val EditorFontSize = intPreferencesKey("editor_font_size")
        val ShowRhymes = booleanPreferencesKey("show_rhymes")
        val OnDeviceAi = booleanPreferencesKey("on_device_ai")
        val LastSyncedAt = longPreferencesKey("last_synced_at")
        val SyncCursor = stringPreferencesKey("sync_cursor")
    }
}
