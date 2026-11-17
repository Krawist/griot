package com.griot.core.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val ItemHeight = 44.dp
private val ItemRadius = 10.dp

/**
 * One action of a [GriotMenu].
 *
 * @param danger Whether it destroys something (« Supprimer »): drawn in the accent text colour.
 * @param enabled Whether it can be tapped; a disabled action is drawn muted.
 */
@Composable
fun GriotMenuItem(label: String, onClick: () -> Unit, danger: Boolean = false, enabled: Boolean = true) {
    val palette = GriotTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ItemHeight)
            .clip(RoundedCornerShape(ItemRadius))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = GriotSpacing.m),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = label,
            style = GriotTheme.type.body,
            color = when {
                !enabled -> palette.muted
                danger -> palette.accentText
                else -> palette.ink
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
