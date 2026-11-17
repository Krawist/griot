package com.griot.feature.settings

import com.griot.core.ai.AiStatus
import com.griot.core.ai.online.KeyCheck
import com.griot.core.data.model.AppLanguage
import com.griot.core.data.model.ExportFile
import com.griot.core.data.model.PreferredAi
import com.griot.core.data.preferences.UserPreferencesRepository

/**
 * What the settings screen shows.
 *
 * @property language Language of the interface, the phone's until the user picks one.
 * @property authorName The author's name, signing the exports; empty until it is given.
 * @property fontSize Size of the lyrics in the editor, in sp.
 * @property onDeviceAi Whether the AI help may use the model on the phone.
 * @property onDeviceStatus Whether the phone's model can answer, downloads, or this phone cannot have it.
 * @property onlineAi Whether the AI help may use the online model, once a key is saved.
 * @property preferredAi The model the AI help asks first, when both can answer.
 * @property usableAi The AIs the help can use, the phone's first: allowed, and the phone able to have its model or a
 *   key saved. The preferred one is chosen only among two.
 * @property apiKeySet Whether a key of the online model is saved.
 * @property checkingKey Whether Anthropic is being asked about a pasted key.
 * @property keyCheck What Anthropic said of the last key pasted; `null` until one is, and once the sheet is closed.
 * @property externalMusic Whether Griot controls the music of other apps (the notification access, GRI-53).
 * @property export Every song exported as Markdown, while its share sheet shows; `null` otherwise.
 */
internal data class SettingsUiState(
    val language: AppLanguage,
    val authorName: String = "",
    val fontSize: Int = UserPreferencesRepository.DEFAULT_EDITOR_FONT_SIZE,
    val onDeviceAi: Boolean = true,
    val onDeviceStatus: AiStatus = AiStatus.UNAVAILABLE,
    val onlineAi: Boolean = true,
    val preferredAi: PreferredAi = PreferredAi.DEVICE,
    val usableAi: List<PreferredAi> = emptyList(),
    val apiKeySet: Boolean = false,
    val checkingKey: Boolean = false,
    val keyCheck: KeyCheck? = null,
    val externalMusic: Boolean = false,
    val export: ExportFile? = null,
)
