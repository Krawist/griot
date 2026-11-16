package com.griot.core.data.model

/** Writing language of a song: it picks the phonetic engine and the language the AI answers in. */
enum class SongLanguage {
    FRENCH,
    ENGLISH,

    /** The dominant language of the song decides (GRI-18). */
    AUTOMATIC,
}
