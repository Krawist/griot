package com.griot.core.design.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val MenuWidth = 240.dp
private val MenuRadius = 16.dp

/**
 * A menu anchored to the « ⋯ » button that opens it: a rounded card on the sheet colour, one [GriotMenuItem] per
 * action. Place it in the same `Box` as its button.
 */
@Composable
fun GriotMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = GriotTheme.palette
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.widthIn(min = MenuWidth).padding(horizontal = GriotSpacing.xs + 2.dp),
        shape = RoundedCornerShape(MenuRadius),
        containerColor = palette.sheet,
        shadowElevation = GriotSpacing.l,
        border = BorderStroke(1.dp, palette.line),
        content = content,
    )
}
