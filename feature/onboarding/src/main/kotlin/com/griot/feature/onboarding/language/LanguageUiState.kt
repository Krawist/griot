package com.griot.feature.onboarding.language

import com.griot.core.data.model.AppLanguage

/**
 * What the language screen shows.
 *
 * @property selected The language picked: [AppLanguage.FRENCH] or [AppLanguage.ENGLISH], never the phone's.
 */
internal data class LanguageUiState(val selected: AppLanguage)
