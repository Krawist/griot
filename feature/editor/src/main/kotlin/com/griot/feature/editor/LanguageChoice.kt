package com.griot.feature.editor

/** A choice of the « Langue de la chanson » sheet: a language set by hand, or the automatic mode. */
internal enum class LanguageChoice {
    FRENCH,
    ENGLISH,

    /** The dominant language of the lyrics, detected again as they change. */
    AUTOMATIC,
}
