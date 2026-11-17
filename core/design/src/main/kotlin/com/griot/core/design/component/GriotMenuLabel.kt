package com.griot.core.design.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/** The title of a group of [GriotMenuItem]s in a [GriotMenu], in upper case. */
@Composable
fun GriotMenuLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = GriotTheme.type.sectionLabel,
        color = GriotTheme.palette.muted,
        modifier = Modifier.padding(
            start = GriotSpacing.m,
            top = GriotSpacing.s,
            end = GriotSpacing.m,
            bottom = GriotSpacing.xs,
        ),
    )
}
