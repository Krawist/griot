package com.griot.core.data.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.griot.core.data.model.AppLanguage
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [AppLanguageRepository], on Android's per-app language API (AppCompat below 13). */
@Singleton
internal class AppLanguageRepositoryImpl @Inject constructor() : AppLanguageRepository {
    override fun current(): AppLanguage = when (AppCompatDelegate.getApplicationLocales().toLanguageTags()) {
        FRENCH_TAG -> AppLanguage.FRENCH
        ENGLISH_TAG -> AppLanguage.ENGLISH
        else -> AppLanguage.SYSTEM
    }

    override fun set(language: AppLanguage) {
        val tags = when (language) {
            AppLanguage.SYSTEM -> ""
            AppLanguage.FRENCH -> FRENCH_TAG
            AppLanguage.ENGLISH -> ENGLISH_TAG
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags))
    }

    private companion object {
        const val FRENCH_TAG = "fr"
        const val ENGLISH_TAG = "en"
    }
}
