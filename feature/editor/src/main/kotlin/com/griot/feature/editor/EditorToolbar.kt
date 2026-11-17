package com.griot.feature.editor

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griot.core.design.R as DesignR
import com.griot.core.design.glass.GlassSurface
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/** Height of the toolbar, which the lyrics scroll clear of. */
internal val EditorToolbarHeight = 64.dp

private val ToolbarElevation = 12.dp
private val ItemRadius = 18.dp

/**
 * The floating glass toolbar of the editor: a new section, the rhymes shown or hidden, the AI help on the verse (in
 * orange), and the versions.
 */
@Composable
internal fun EditorToolbar(
    showRhymes: Boolean,
    onAddSection: () -> Unit,
    onShowRhymesChange: (Boolean) -> Unit,
    onAiHelp: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        shape = RoundedCornerShape(GriotRadius.sheet),
        strong = true,
        elevation = ToolbarElevation,
        modifier = modifier.fillMaxWidth().height(EditorToolbarHeight),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs),
        ) {
            ToolbarItem(
                icon = DesignR.drawable.ic_section,
                label = stringResource(R.string.editor_tool_section),
                selected = false,
                modifier = Modifier.weight(1f).clickable(role = Role.Button, onClick = onAddSection),
            )
            ToolbarItem(
                icon = DesignR.drawable.ic_rhymes,
                label = stringResource(R.string.editor_tool_rhymes),
                selected = showRhymes,
                modifier = Modifier
                    .weight(1f)
                    .toggleable(value = showRhymes, role = Role.Switch, onValueChange = onShowRhymesChange),
            )
            ToolbarItem(
                icon = DesignR.drawable.ic_ai,
                label = stringResource(R.string.editor_tool_ai),
                selected = false,
                ai = true,
                modifier = Modifier.weight(1f).clickable(role = Role.Button, onClick = onAiHelp),
            )
            ToolbarItem(
                icon = DesignR.drawable.ic_history,
                label = stringResource(R.string.editor_tool_versions),
                selected = false,
                modifier = Modifier.weight(1f).clickable(role = Role.Button, onClick = onOpenHistory),
            )
        }
    }
}

// An item of the toolbar, turquoise while its mode is on; the AI item is always orange.
@Composable
private fun ToolbarItem(
    @DrawableRes icon: Int,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    ai: Boolean = false,
) {
    val palette = GriotTheme.palette
    val motion = GriotTheme.motion
    val fill by animateColorAsState(
        targetValue = when {
            ai -> palette.ai
            selected -> palette.syncSoft
            else -> Color.Transparent
        },
        animationSpec = motion.colorChange(),
        label = "tool fill",
    )
    val content by animateColorAsState(
        targetValue = when {
            ai -> palette.onAi
            selected -> palette.syncText
            else -> palette.ink2
        },
        animationSpec = motion.colorChange(),
        label = "tool content",
    )
    Column(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(ItemRadius)).background(fill),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = content)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected || ai) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = content,
        )
    }
}
