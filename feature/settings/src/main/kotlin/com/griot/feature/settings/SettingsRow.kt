package com.griot.feature.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val IconSize = 36.dp
private val GlyphSize = 20.dp
private val RowHeight = 58.dp
private const val DISABLED_ALPHA = 0.5f

/**
 * A row of a [SettingsGroup]: a tinted icon box, a label and its sub-line, then a value, a switch or a button.
 *
 * @param letter The letters or symbol in the icon box, unless [icon] is given.
 * @param first Whether it is the first row of its group: it has no line above.
 * @param enabled A row not available yet is dimmed and does nothing.
 * @param onClick What a tap on the row does; `null` when the row only shows a value.
 */
@Suppress("LongParameterList") // What the row shows, its state and its action.
@Composable
internal fun SettingsRow(
    letter: String,
    tone: SettingsTone,
    label: String,
    sub: String,
    first: Boolean,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val palette = GriotTheme.palette
    if (!first) HorizontalDivider(thickness = 1.dp, color = palette.line)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .padding(horizontal = GriotSpacing.l - 2.dp, vertical = GriotSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
    ) {
        IconBox(letter = letter, icon = icon, tone = tone)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                style = GriotTheme.type.textButton,
                color = palette.ink,
            )
            Text(
                text = sub,
                style = GriotTheme.type.caption,
                color = palette.muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing()
    }
}

// The 36 dp box of the row's icon, tinted after its tone.
@Composable
private fun IconBox(letter: String, @DrawableRes icon: Int?, tone: SettingsTone) {
    val (background, foreground) = tone.colors()
    Box(
        modifier = Modifier
            .size(IconSize)
            .clip(RoundedCornerShape(GriotRadius.control))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = foreground,
                modifier = Modifier.size(GlyphSize),
            )
        } else {
            Text(
                text = letter,
                style = GriotTheme.type.iconLetter,
                color = foreground,
            )
        }
    }
}

@Composable
private fun SettingsTone.colors(): Pair<Color, Color> {
    val palette = GriotTheme.palette
    return when (this) {
        SettingsTone.BRAND -> palette.accentSoft to palette.accentText
        SettingsTone.SYNC -> palette.syncSoft to palette.syncText
        SettingsTone.AI -> palette.aiSoft to palette.aiText
    }
}

/** The row's current value in muted text, with a chevron when the row opens something. */
@Composable
internal fun SettingsValue(value: String, chevron: Boolean = true) {
    val palette = GriotTheme.palette
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs)) {
        if (value.isNotEmpty()) {
            Text(text = value, style = GriotTheme.type.body, color = palette.muted, maxLines = 1)
        }
        if (chevron) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_chevron),
                contentDescription = null,
                tint = palette.muted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/** A small tinted button at the end of a row, such as « Synchroniser ». */
@Composable
internal fun SettingsButton(label: String, tone: SettingsTone, enabled: Boolean, onClick: () -> Unit) {
    val (background, foreground) = tone.colors()
    val shape = RoundedCornerShape(17.dp)
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(shape)
            .background(background, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = GriotSpacing.m),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = GriotTheme.type.smallStrong,
            color = foreground,
        )
    }
}
