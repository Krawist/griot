package com.griot.core.data.song

import com.griot.core.data.model.SongLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [WordCorrectionRepository] for tests: [corrections] holds the words corrected, by language. */
class FakeWordCorrectionRepository : WordCorrectionRepository {
    val corrections = MutableStateFlow<Map<SongLanguage, Map<String, Int>>>(emptyMap())

    override fun observeCorrections(language: SongLanguage) = corrections.map { it[language].orEmpty() }

    override suspend fun correct(language: SongLanguage, word: String, syllables: Int) {
        val words = corrections.value[language].orEmpty() + (word.lowercase() to syllables)
        corrections.value = corrections.value + (language to words)
    }
}
