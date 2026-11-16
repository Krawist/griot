package com.griot.core.phonetics.paste

/** Kind of a block of pasted lyrics, from its label (« [Refrain] », « Couplet 2 : », « Bridge »). */
enum class BlockKind {
    INTRO,
    VERSE,
    PRE_CHORUS,
    CHORUS,
    BRIDGE,
    OUTRO,
}
