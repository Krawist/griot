package com.griot.feature.settings

import com.griot.core.data.model.AppLanguage
import com.griot.core.data.model.ExportFile
import com.griot.core.data.preferences.UserPreferencesRepository

/**
 * What the settings screen shows.
 *
 * @property language Language of the interface, the phone's until the user picks one.
 * @property authorName The author's name, signing the exports; empty until it is given.
 * @property fontSize Size of the lyrics in the editor, in sp.
 * @property export Every song exported as Markdown, while its share sheet shows; `null` otherwise.
 */
internal data class SettingsUiState(
    val language: AppLanguage,
    val authorName: String = "",
    val fontSize: Int = UserPreferencesRepository.DEFAULT_EDITOR_FONT_SIZE,
    val export: ExportFile? = null,
)
