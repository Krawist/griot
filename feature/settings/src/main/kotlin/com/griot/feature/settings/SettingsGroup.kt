package com.griot.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/** A titled glass card of settings rows, the rows separated by a line. */
@Composable
internal fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.card)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            style = GriotTheme.type.sectionLabel,
            color = palette.muted,
            modifier = Modifier.padding(horizontal = GriotSpacing.s - 2.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(palette.glass, shape)
                .border(1.dp, palette.line, shape),
            content = content,
        )
    }
}
