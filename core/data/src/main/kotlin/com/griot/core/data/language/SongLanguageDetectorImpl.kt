package com.griot.core.data.language

import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.griot.core.data.model.SongLanguage
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Default implementation for [SongLanguageDetector], on ML Kit's language identification, with its model on board. */
@Singleton
internal class SongLanguageDetectorImpl @Inject constructor(private val identifier: LanguageIdentifier) :
    SongLanguageDetector {
    override suspend fun detect(text: String): SongLanguage? {
        if (text.isBlank()) return null
        val tag = suspendCancellableCoroutine { continuation ->
            // Run where the task completes: no need for the main thread.
            identifier.identifyLanguage(text).addOnCompleteListener(Runnable::run) { task ->
                continuation.resume(if (task.isSuccessful) task.result else null)
            }
        }
        return when (tag) {
            FRENCH_TAG -> SongLanguage.FRENCH
            ENGLISH_TAG -> SongLanguage.ENGLISH
            else -> null
        }
    }

    private companion object {
        const val FRENCH_TAG = "fr"
        const val ENGLISH_TAG = "en"
    }
}
