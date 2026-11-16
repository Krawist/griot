package com.griot.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Small user settings that are not songs. The Room database (GRI-05) holds the songs, versions and ideas. */
@Singleton
class UserPreferencesRepository @Inject internal constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { it[OnboardingCompleted] ?: false }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { it[OnboardingCompleted] = true }
    }

    private companion object {
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
    }
}
