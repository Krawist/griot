package com.griot.core.data.song

import android.content.res.Resources
import com.griot.core.data.R
import com.griot.core.data.model.SectionKind

/** The label in words: the author's name, else « Couplet 2 » or « Refrain », in the language of [resources]. */
fun SectionLabel.words(resources: Resources): String {
    val kindName = resources.getString(kind.nameRes())
    return name ?: number?.let { resources.getString(R.string.section_label_numbered, kindName, it) } ?: kindName
}

/** The word for a kind of section, « Couplet », « Refrain »… */
fun SectionKind.nameRes(): Int = when (this) {
    SectionKind.INTRO -> R.string.section_kind_intro
    SectionKind.VERSE -> R.string.section_kind_verse
    SectionKind.CHORUS -> R.string.section_kind_chorus
    SectionKind.BRIDGE -> R.string.section_kind_bridge
    SectionKind.OUTRO -> R.string.section_kind_outro
    SectionKind.OTHER -> R.string.section_kind_other
}
