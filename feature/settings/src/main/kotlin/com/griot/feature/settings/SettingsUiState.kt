package com.griot.feature.settings

import com.griot.core.data.model.AppLanguage

/**
 * What the settings screen shows.
 *
 * @property language Language of the interface, the phone's until the user picks one.
 */
internal data class SettingsUiState(val language: AppLanguage)
