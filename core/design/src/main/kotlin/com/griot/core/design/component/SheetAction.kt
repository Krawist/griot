package com.griot.core.design.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val ActionHeight = 56.dp

/**
 * One action of a [GriotSheet]: its icon and its label, in [color] (ink, or magenta text for a delete).
 *
 * @param enabled Whether it can be tapped; a disabled action is drawn muted.
 */
@Composable
fun SheetAction(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    color: Color = GriotTheme.palette.ink,
    enabled: Boolean = true,
) {
    val tint = if (enabled) color else GriotTheme.palette.muted
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ActionHeight)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.l),
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = tint)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = tint,
        )
    }
}
