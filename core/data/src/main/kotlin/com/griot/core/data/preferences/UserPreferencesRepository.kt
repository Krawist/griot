package com.griot.core.data.preferences

import com.griot.core.data.model.SongLanguage
import kotlinx.coroutines.flow.Flow

/** Small user settings that are not songs, kept on the phone. The Room database holds the songs, versions and ideas. */
interface UserPreferencesRepository {
    /** Whether the onboarding was completed once; `false` on a new install. */
    val onboardingCompleted: Flow<Boolean>

    /** First name or artist name signing the exported texts; `null` until the user gives one. */
    val authorName: Flow<String?>

    /** Writing language of new songs, chosen in the onboarding; [SongLanguage.AUTOMATIC] until then. */
    val defaultSongLanguage: Flow<SongLanguage>

    /** Records that the onboarding was completed: the next launches open the library. */
    suspend fun setOnboardingCompleted()

    /** Saves the [name] signing the exported texts, as written. */
    suspend fun setAuthorName(name: String)

    /** Saves the writing [language] of new songs. */
    suspend fun setDefaultSongLanguage(language: SongLanguage)
}
