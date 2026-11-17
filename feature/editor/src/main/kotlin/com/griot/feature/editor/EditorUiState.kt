package com.griot.feature.editor

import com.griot.core.data.model.ExportFile
import com.griot.core.data.model.Idea
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.preferences.UserPreferencesRepository

/**
 * What the editor shows.
 *
 * @property loading Whether the song is still being read.
 * @property missing Whether the song no longer exists or went to the trash: the editor closes.
 * @property title The song's title; empty for a song not named yet.
 * @property language The language the song is written in, chosen or detected: it picks the phonetic engine.
 * @property languageDetected Whether [language] is detected from the lyrics (automatic mode).
 * @property languageChoice What the language sheet shows as chosen: automatic, or the language set by hand.
 * @property sections Sections in order, with their labels.
 * @property focus The verse to focus next, until its field took the focus; `null` otherwise.
 * @property fontSize Size of the lyrics, in sp, before the system font scale.
 * @property showRhymes Whether the rhymes are coloured.
 * @property export The song exported as Markdown, while its share sheet shows; `null` otherwise.
 * @property ideas The ideas linked to the song, newest first, which its « ⋯ » menu offers to add as verses.
 * @property audioCount How many audios the song has: the count on the top bar's audios button.
 * @property selection The selected verse, which its sheet acts on: the one being edited, or the one whose letter or
 * count was tapped; `null` when none is.
 * @property pasteUndo The last paste split into sections, while its undo bar shows; `null` otherwise.
 * @property conflicts The ids of the verses changed on two devices whose conflict the author did not look at, oldest
 * first; a notice opens the history of the first one.
 */
internal data class EditorUiState(
    val loading: Boolean = true,
    val missing: Boolean = false,
    val title: String = "",
    val language: SongLanguage = SongLanguage.FRENCH,
    val languageDetected: Boolean = false,
    val languageChoice: LanguageChoice = LanguageChoice.FRENCH,
    val sections: List<EditorSection> = emptyList(),
    val focus: VerseFocus? = null,
    val fontSize: Int = UserPreferencesRepository.DEFAULT_EDITOR_FONT_SIZE,
    val showRhymes: Boolean = true,
    val pasteUndo: PasteUndo? = null,
    val selection: VerseSelection? = null,
    val audioCount: Int = 0,
    val export: ExportFile? = null,
    val ideas: List<Idea> = emptyList(),
    val conflicts: List<String> = emptyList(),
)
