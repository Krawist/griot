package com.griot.core.design.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.griot.core.design.R
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val CardHeight = 76.dp
private val CardRadius = 20.dp
private val BadgeSize = 44.dp
private val RadioSize = 26.dp
private val RadioRing = 2.dp

/** The options of one choice, read out as a group named [label], one [ChoiceCard] each. */
@Composable
fun ChoiceGroup(label: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.selectableGroup().semantics { contentDescription = label },
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.m),
        content = content,
    )
}

/**
 * One option of a choice, as in the onboarding: a glass card with its badge, its [title] and its [hint], and a round
 * mark, filled in magenta with a check while it is [selected].
 *
 * @param badge The short text in the badge (« FR »), unless [icon] is given; no badge without either.
 * @param icon The icon in the badge.
 * @param hint What the option does, under its title; none when `null`.
 */
@Composable
fun ChoiceCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    @DrawableRes icon: Int? = null,
    hint: String? = null,
) {
    val palette = GriotTheme.palette
    val motion = GriotTheme.motion
    val shape = RoundedCornerShape(CardRadius)
    val border by animateColorAsState(
        targetValue = if (selected) palette.accent else palette.line,
        animationSpec = motion.colorChange(),
        label = "choice border",
    )
    val fill by animateColorAsState(
        targetValue = if (selected) palette.glassSelected else palette.glass,
        animationSpec = motion.colorChange(),
        label = "choice fill",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CardHeight)
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .background(fill, shape)
            .border(width = if (selected) 2.dp else 1.dp, color = border, shape = shape)
            .padding(horizontal = GriotSpacing.l, vertical = GriotSpacing.m + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m + 2.dp),
    ) {
        if (badge != null || icon != null) Badge(badge = badge, icon = icon)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = GriotTheme.type.button, color = palette.ink)
            hint?.let { Text(text = it, style = GriotTheme.type.description, color = palette.muted) }
        }
        Radio(selected = selected)
    }
}

@Composable
private fun Badge(badge: String?, @DrawableRes icon: Int?) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.row)
    Box(
        modifier = Modifier
            .size(BadgeSize)
            .background(palette.chip, shape)
            .border(1.dp, palette.line, shape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            icon != null -> Icon(painter = painterResource(icon), contentDescription = null, tint = palette.ink)
            badge != null -> Text(text = badge, style = GriotTheme.type.code, color = palette.ink)
        }
    }
}

@Composable
private fun Radio(selected: Boolean) {
    val palette = GriotTheme.palette
    val fill by animateColorAsState(
        targetValue = if (selected) palette.accentFill else palette.accentFill.copy(alpha = 0f),
        animationSpec = GriotTheme.motion.colorChange(),
        label = "choice radio",
    )
    Box(
        modifier = Modifier
            .size(RadioSize)
            .background(fill, CircleShape)
            .then(if (selected) Modifier else Modifier.border(RadioRing, palette.radioOff, CircleShape)),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = palette.onAccent)
        }
    }
}
