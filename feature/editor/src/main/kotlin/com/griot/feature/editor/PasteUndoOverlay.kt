package com.griot.feature.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import com.griot.core.design.component.UndoBar
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import io.github.krawist.design.component.AnimatedPresence

/** « 4 sections créées · Annuler » at the bottom of the editor, for a few seconds after a paste split into sections. */
@Composable
internal fun PasteUndoOverlay(undo: PasteUndo?, onUndo: () -> Unit, onDismissed: () -> Unit) {
    val motion = GriotTheme.motion
    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
        AnimatedPresence(
            value = undo,
            enter = motion.barEnter(),
            exit = motion.barExit(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(GriotSpacing.l),
        ) { shown ->
            UndoBar(
                message = pluralStringResource(R.plurals.editor_paste_sections, shown.sectionCount, shown.sectionCount),
                onUndo = onUndo,
                onDismissed = onDismissed,
            )
        }
    }
}
