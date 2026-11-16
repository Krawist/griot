package com.griot.feature.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.SectionKind

/** The label in words: the author's name, else « Couplet 2 » or « Refrain ». */
@Composable
internal fun SectionLabel.text(): String {
    val kindName = stringResource(kind.nameRes())
    return name ?: number?.let { stringResource(R.string.editor_label_numbered, kindName, it) } ?: kindName
}

/** The word for a kind of section, « Couplet », « Refrain »… */
internal fun SectionKind.nameRes(): Int = when (this) {
    SectionKind.INTRO -> R.string.editor_kind_intro
    SectionKind.VERSE -> R.string.editor_kind_verse
    SectionKind.CHORUS -> R.string.editor_kind_chorus
    SectionKind.BRIDGE -> R.string.editor_kind_bridge
    SectionKind.OUTRO -> R.string.editor_kind_outro
    SectionKind.OTHER -> R.string.editor_kind_other
}
