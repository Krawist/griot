package com.griot.feature.settings

import com.griot.core.data.model.AppLanguage
import com.griot.core.data.model.ExportFile

/**
 * What the settings screen shows.
 *
 * @property language Language of the interface, the phone's until the user picks one.
 * @property export Every song exported as Markdown, while its share sheet shows; `null` otherwise.
 */
internal data class SettingsUiState(val language: AppLanguage, val export: ExportFile? = null)
