package com.griot.feature.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction

/** The « ⋯ » menu of the editor: for now, the export of the song as Markdown. */
@Composable
internal fun EditorMenuSheet(onExport: () -> Unit, onDismiss: () -> Unit) {
    GriotSheet(onDismiss = onDismiss) {
        SheetAction(
            icon = DesignR.drawable.ic_share,
            label = stringResource(R.string.editor_export),
            onClick = onExport,
        )
    }
}
