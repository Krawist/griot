package com.griot.core.data.song

import com.griot.core.data.model.NewSection
import com.griot.core.data.model.SectionKind
import com.griot.core.phonetics.paste.BlockKind
import com.griot.core.phonetics.paste.PastedBlock

/**
 * The sections to create from the blocks of a paste. A plain block is a verse; a pre-chorus, which Griot has no
 * kind for, is an « other » section named as its label was written.
 */
fun List<PastedBlock>.toNewSections(): List<NewSection> = map { block ->
    NewSection(
        kind = when (block.kind) {
            BlockKind.INTRO -> SectionKind.INTRO
            BlockKind.VERSE, null -> SectionKind.VERSE
            BlockKind.PRE_CHORUS -> SectionKind.OTHER
            BlockKind.CHORUS -> SectionKind.CHORUS
            BlockKind.BRIDGE -> SectionKind.BRIDGE
            BlockKind.OUTRO -> SectionKind.OUTRO
        },
        name = block.label.takeIf { block.kind == BlockKind.PRE_CHORUS },
        lines = block.lines,
        repeatOf = block.repeatOf,
    )
}
