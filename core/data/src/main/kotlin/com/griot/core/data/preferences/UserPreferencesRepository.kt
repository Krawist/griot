package com.griot.core.data.preferences

import com.griot.core.data.model.SongLanguage
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** Small user settings that are not songs, kept on the phone. The Room database holds the songs, versions and ideas. */
interface UserPreferencesRepository {
    /** Whether the onboarding was completed once; `false` on a new install. */
    val onboardingCompleted: Flow<Boolean>

    /** First name or artist name signing the exported texts; `null` until the user gives one. */
    val authorName: Flow<String?>

    /** Writing language of new songs, chosen in the onboarding; [SongLanguage.AUTOMATIC] until then. */
    val defaultSongLanguage: Flow<SongLanguage>

    /** Size of the lyrics in the editor, in sp, for every song; [DEFAULT_EDITOR_FONT_SIZE] until it is changed. */
    val editorFontSize: Flow<Int>

    /** Whether the editor colours the rhymes; `true` until the user hides them. */
    val showRhymes: Flow<Boolean>

    /** When the last sync with the remote store succeeded; `null` until the first one. */
    val lastSyncedAt: Flow<Instant?>

    /** Where the last sync stopped reading the remote changes, as the remote store gives it; `null` to read all. */
    val syncCursor: Flow<String?>

    /** Records that the onboarding was completed: the next launches open the library. */
    suspend fun setOnboardingCompleted()

    /** Saves the [name] signing the exported texts, as written. */
    suspend fun setAuthorName(name: String)

    /** Saves the writing [language] of new songs. */
    suspend fun setDefaultSongLanguage(language: SongLanguage)

    /** Saves the [size] of the lyrics in the editor, in sp. */
    suspend fun setEditorFontSize(size: Int)

    /** Shows or hides the colours of the rhymes in the editor. */
    suspend fun setShowRhymes(show: Boolean)

    /** Records a successful sync at [at], and the [cursor] of the remote changes read so far. */
    suspend fun setSynced(at: Instant, cursor: String?)

    companion object {
        /** Size of the lyrics, in sp, before the user changes it. */
        const val DEFAULT_EDITOR_FONT_SIZE = 18
    }
}
