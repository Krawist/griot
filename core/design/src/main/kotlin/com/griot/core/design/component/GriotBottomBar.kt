package com.griot.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.griot.core.design.R
import com.griot.core.design.glass.GlassSurface
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val BarHeight = 68.dp
private val BarBottom = 24.dp
private val BarElevation = 16.dp
private val IconSize = 22.dp
private val IconLabelGap = 3.dp

/**
 * Floating glass bottom bar holding the [tabs], each an icon over its label. The selected tab is drawn in magenta.
 *
 * @param selectedTab Index of the selected tab in [tabs], or -1 when none is.
 */
@Composable
fun GriotBottomBar(
    tabs: List<BottomBarTab>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.bar)
    GlassSurface(
        shape = shape,
        strong = true,
        elevation = BarElevation,
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = GriotSpacing.l, end = GriotSpacing.l, bottom = BarBottom)
            .fillMaxWidth()
            .height(BarHeight),
    ) {
        Row(modifier = Modifier.fillMaxSize().selectableGroup()) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedTab
                val color = if (selected) palette.accentText else palette.muted
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(shape)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onTabSelected(index) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(IconLabelGap, Alignment.CenterVertically),
                ) {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(IconSize),
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = color,
                    )
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GriotBottomBarPreview() {
    GriotTheme {
        GriotBottomBar(
            tabs = listOf(
                BottomBarTab(icon = R.drawable.ic_nav_songs, label = "Chansons"),
                BottomBarTab(icon = R.drawable.ic_nav_ideas, label = "Idées"),
                BottomBarTab(icon = R.drawable.ic_nav_settings, label = "Réglages"),
            ),
            selectedTab = 0,
            onTabSelected = {},
        )
    }
}
