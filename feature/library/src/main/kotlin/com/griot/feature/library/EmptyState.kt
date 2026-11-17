package com.griot.feature.library

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.griot.core.design.component.GriotButton
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val ArtSize = 132.dp
private val ArtShape = RoundedCornerShape(36.dp)
private val ButtonHeight = 52.dp
private val BodyWidth = 290.dp

/**
 * What a list says when it has nothing to show (« Vide »): an [icon] in a glass tile, a [title], what to do in [body],
 * then the [primary] button and the [secondary] one.
 *
 * @param tint The icon's colour; none keeps the icon's own colours, such as the logo's.
 * @param top The room above, larger on a screen with little else.
 */
@Suppress("LongParameterList") // What it shows, and its two buttons.
@Composable
internal fun EmptyState(
    @DrawableRes icon: Int,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    primary: EmptyAction? = null,
    secondary: EmptyAction? = null,
    top: Dp = GriotSpacing.xxl * 2,
) {
    val palette = GriotTheme.palette
    Column(
        modifier = modifier.fillMaxWidth().padding(top = top, bottom = GriotSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.m),
    ) {
        Box(
            modifier = Modifier.size(ArtSize).background(palette.glass, ArtShape).border(1.dp, palette.line, ArtShape),
            contentAlignment = Alignment.Center,
        ) {
            if (tint != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(64.dp),
                )
            } else {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(96.dp),
                )
            }
        }
        Text(
            text = title,
            style = GriotTheme.type.sheetTitle,
            color = palette.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = GriotSpacing.s),
        )
        Text(
            text = body,
            style = GriotTheme.type.description,
            color = palette.ink2,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = BodyWidth),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = GriotSpacing.xl, vertical = GriotSpacing.s),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        ) {
            primary?.let { GriotButton(text = it.label, onClick = it.onClick) }
            secondary?.let { SecondaryButton(action = it) }
        }
    }
}

@Composable
private fun SecondaryButton(action: EmptyAction) {
    val palette = GriotTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .clip(CircleShape)
            .background(palette.glass, CircleShape)
            .border(1.dp, palette.line, CircleShape)
            .clickable(role = Role.Button, onClick = action.onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = action.label, style = GriotTheme.type.button, color = palette.ink)
    }
}
