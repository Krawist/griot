package com.griot.core.data.language

import com.griot.core.data.model.AppLanguage

/** In-memory [AppLanguageRepository] for tests: it remembers the last language set. */
class FakeAppLanguageRepository(var language: AppLanguage = AppLanguage.SYSTEM) : AppLanguageRepository {
    override fun current(): AppLanguage = language

    override fun set(language: AppLanguage) {
        this.language = language
    }
}
