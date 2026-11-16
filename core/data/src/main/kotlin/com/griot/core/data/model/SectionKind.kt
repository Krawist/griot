package com.griot.core.data.model

/** Kind of a section of a song; sections of the same kind are numbered in order (« Couplet 1 », « Couplet 2 »). */
enum class SectionKind {
    INTRO,
    VERSE,
    CHORUS,
    BRIDGE,
    OUTRO,
    OTHER,
}
