package com.griot.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.griot.core.design.theme.DarkPalette
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/**
 * Bottom sheet of Griot: it rises from the bottom over a scrim, and stays above the keyboard. Its window draws the
 * system bars' icons for Griot's theme, which may differ from the phone's.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GriotSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val palette = GriotTheme.palette
    val light = palette !== DarkPalette
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = light,
            isAppearanceLightNavigationBars = light,
        ),
        containerColor = palette.sheet,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.track) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = GriotSpacing.xl, end = GriotSpacing.xl, bottom = GriotSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
            content = content,
        )
    }
}
