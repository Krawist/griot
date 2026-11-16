package com.griot.core.data.language

import com.griot.core.data.model.AppLanguage

/** Language of the interface, kept by Android per app: it applies at once and survives a restart. */
interface AppLanguageRepository {
    /** The language chosen for the interface; [AppLanguage.SYSTEM] until the user picks one. */
    fun current(): AppLanguage

    /** Applies [language] to the interface without restarting the app; the open screens are redrawn. */
    fun set(language: AppLanguage)
}
