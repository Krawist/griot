package com.griot.core.ai

import com.griot.core.phonetics.PhoneticLanguage

/**
 * A whole song the tidy-up asks about (GRI-59).
 *
 * @property language The language the song is written in.
 * @property lyrics Its first verses, in order: enough to read its themes and style.
 * @property folders The names of the author's folders, among which a classification picks one.
 * @property genres The names of the genres, among which a classification picks one.
 */
data class SongRequest(
    val language: PhoneticLanguage,
    val lyrics: List<String>,
    val folders: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
)
