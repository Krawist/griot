package com.griot.core.data.model

/**
 * Language a song is written in: it picks the phonetic engine and the language the AI answers in. A song always has
 * one; in automatic mode it is the detected one (GRI-54).
 */
enum class SongLanguage {
    FRENCH,
    ENGLISH,
}
