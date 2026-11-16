package com.griot.core.data.language

import com.griot.core.data.model.SongLanguage

/** Finds the dominant language of a text, on the phone: the text is never sent anywhere. */
interface SongLanguageDetector {
    /**
     * The dominant language of [text], a whole song: [SongLanguage.FRENCH] or [SongLanguage.ENGLISH]; `null` when it
     * is neither, or too short to tell.
     */
    suspend fun detect(text: String): SongLanguage?
}
