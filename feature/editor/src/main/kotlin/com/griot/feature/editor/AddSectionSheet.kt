package com.griot.feature.editor

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.SectionKind
import com.griot.core.data.song.nameRes
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction
import com.griot.core.design.theme.GriotTheme

// The kinds in the order a song usually follows.
private val Kinds = listOf(
    SectionKind.VERSE,
    SectionKind.CHORUS,
    SectionKind.BRIDGE,
    SectionKind.INTRO,
    SectionKind.OUTRO,
    SectionKind.OTHER,
)

/** The kinds of section that « Ajouter une section » offers. */
@Composable
internal fun AddSectionSheet(onAdd: (SectionKind) -> Unit, onDismiss: () -> Unit) {
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.editor_add_section),
            style = GriotTheme.type.sheetTitle,
            color = GriotTheme.palette.ink,
        )
        Kinds.forEach { kind ->
            SheetAction(icon = DesignR.drawable.ic_add, label = stringResource(kind.nameRes()), onClick = {
                onAdd(kind)
            })
        }
    }
}
