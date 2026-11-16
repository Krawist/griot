package com.griot.core.data.language

import com.griot.core.data.model.SongLanguage

/** [SongLanguageDetector] for tests: it finds [language] in any text that is not blank, and records the texts. */
class FakeSongLanguageDetector(var language: SongLanguage? = null) : SongLanguageDetector {
    val texts = mutableListOf<String>()

    override suspend fun detect(text: String): SongLanguage? {
        texts += text
        return language.takeIf { text.isNotBlank() }
    }
}
