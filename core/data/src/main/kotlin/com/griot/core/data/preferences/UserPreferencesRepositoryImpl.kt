package com.griot.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { it[OnboardingCompleted] = true }
    }

    override suspend fun setAuthorName(name: String) {
        dataStore.edit { it[AuthorName] = name }
    }

    private companion object {
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val AuthorName = stringPreferencesKey("author_name")
    }
}
