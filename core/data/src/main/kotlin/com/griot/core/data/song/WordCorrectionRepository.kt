package com.griot.core.data.song

import com.griot.core.data.model.SongLanguage
import kotlinx.coroutines.flow.Flow

/** The syllables the user gave to words the dictionaries do not know, kept for every song. */
interface WordCorrectionRepository {
    /** The syllables of the corrected words of [language], by word in lower case. */
    fun observeCorrections(language: SongLanguage): Flow<Map<String, Int>>

    /** Keeps [syllables] for [word] of [language], French or English, in every song. */
    suspend fun correct(language: SongLanguage, word: String, syllables: Int)
}
