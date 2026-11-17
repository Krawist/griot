package com.griot.feature.library.folder

/** The order of the songs of a folder: « Modifiés », « Créés » or « Titre A → Z ». */
internal enum class SongSort {
    /** Last changed first. */
    MODIFIED,

    /** Last created first. */
    CREATED,

    /** By title, from A to Z, the songs without one last. */
    TITLE,
}
