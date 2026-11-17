package com.griot.core.data.preferences

import com.griot.core.data.model.PreferredAi
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.ThemeMode
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** Small user settings that are not songs, kept on the phone. The Room database holds the songs, versions and ideas. */
@Suppress("TooManyFunctions") // One setter per preference.
interface UserPreferencesRepository {
    /** Whether the onboarding was completed once; `false` on a new install. */
    val onboardingCompleted: Flow<Boolean>

    /** First name or artist name signing the exported texts; `null` until the user gives one. */
    val authorName: Flow<String?>

    /** Writing language of new songs, chosen in the onboarding; the phone's language until then. */
    val defaultSongLanguage: Flow<SongLanguage>

    /** Size of the lyrics in the editor, in sp, for every song; [DEFAULT_EDITOR_FONT_SIZE] until it is changed. */
    val editorFontSize: Flow<Int>

    /** Whether the editor colours the rhymes; `true` until the user hides them. */
    val showRhymes: Flow<Boolean>

    /** Whether the AI help may use the model on the phone; `true` until the user turns it off. */
    val onDeviceAi: Flow<Boolean>

    /** Whether the AI help may send the verses to the online model once its key is set; `true` until turned off. */
    val onlineAi: Flow<Boolean>

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

    /** Saves the [size] of the lyrics in the editor, in sp, kept within [EDITOR_FONT_SIZES]. */
    suspend fun setEditorFontSize(size: Int)

    /** Whether Griot is light, dark, or as the phone; [ThemeMode.SYSTEM] until the author picks one. */
    val themeMode: Flow<ThemeMode>

    /** Saves whether Griot is light, dark, or as the phone. */
    suspend fun setThemeMode(mode: ThemeMode)

    /** Shows or hides the colours of the rhymes in the editor. */
    suspend fun setShowRhymes(show: Boolean)

    /** Allows or forbids the AI help to use the model on the phone. */
    suspend fun setOnDeviceAi(allowed: Boolean)

    /** Allows or forbids the AI help to use the online model. */
    suspend fun setOnlineAi(allowed: Boolean)

    /** The model the AI help asks first, among those allowed; [PreferredAi.DEVICE] until the author changes it. */
    val preferredAi: Flow<PreferredAi>

    /** Saves the model the AI help asks first. */
    suspend fun setPreferredAi(preferred: PreferredAi)

    /**
     * The key of the online model, encrypted by a key of the Android Keystore (never the key itself); `null` until one
     * is saved.
     */
    val onlineAiKey: Flow<String?>

    /** Saves the encrypted key of the online model, or forgets it when [secret] is `null`. */
    suspend fun setOnlineAiKey(secret: String?)

    /** Records a successful sync at [at], and the [cursor] of the remote changes read so far. */
    suspend fun setSynced(at: Instant, cursor: String?)

    /** Whether an import asked for a tidy-up (GRI-59), until the author looks at the proposals. */
    val tidyUpRequested: Flow<Boolean>

    /** When the author last looked at the tidy-up proposals; `null` never. */
    val tidyUpReviewedAt: Flow<Instant?>

    /** The id of this phone, made once: a file added here is on this phone only (GRI-51). */
    suspend fun deviceId(): String

    /** Whether the author saw the explanation of « Contrôler la musique des autres apps » (GRI-53). */
    val externalMusicExplained: Flow<Boolean>

    /** The explanation of the music of other apps was shown: it does not come back. */
    suspend fun setExternalMusicExplained()

    /** An import asks for a tidy-up. */
    suspend fun requestTidyUp()

    /** The author looked at the proposals at [at]: the next ones come after an import, or a week later. */
    suspend fun setTidyUpReviewed(at: Instant)

    companion object {
        /** Size of the lyrics, in sp, before the user changes it. */
        const val DEFAULT_EDITOR_FONT_SIZE = 18

        /** The sizes the lyrics can take, in sp. */
        val EDITOR_FONT_SIZES = 14..28
    }
}
