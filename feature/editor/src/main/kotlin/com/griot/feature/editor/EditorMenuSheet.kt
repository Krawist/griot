package com.griot.feature.editor

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.Idea
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction
import com.griot.core.design.theme.GriotTheme

/** The « ⋯ » menu of the editor: the export as Markdown, and the ideas linked to the song, to add as verses. */
@Composable
internal fun EditorMenuSheet(
    ideas: List<Idea>,
    onExport: () -> Unit,
    onAddIdea: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    GriotSheet(onDismiss = onDismiss) {
        SheetAction(
            icon = DesignR.drawable.ic_share,
            label = stringResource(R.string.editor_export),
            onClick = onExport,
        )
        val texts = ideas.filter { it.text.isNotBlank() }
        if (texts.isNotEmpty()) {
            Text(
                text = stringResource(R.string.editor_ideas),
                style = GriotTheme.type.captionStrong,
                color = GriotTheme.palette.muted,
            )
            texts.forEach { idea ->
                SheetAction(icon = DesignR.drawable.ic_add, label = idea.text, onClick = { onAddIdea(idea.text) })
            }
        }
    }
}
