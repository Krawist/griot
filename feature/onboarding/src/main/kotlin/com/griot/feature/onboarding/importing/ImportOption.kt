package com.griot.feature.onboarding.importing

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import io.github.krawist.design.glass.GlassSurface

private val OptionHeight = 76.dp
private val OptionRadius = 20.dp
private val IconBox = 44.dp

/** One way to import texts: its icon on a tinted square, its name and a hint. */
@Suppress("LongParameterList") // The option's look, its texts and its action.
@Composable
internal fun ImportOption(
    @DrawableRes icon: Int,
    tint: Color,
    fill: Color,
    title: String,
    hint: String,
    onClick: () -> Unit,
) {
    val palette = GriotTheme.palette
    GlassSurface(
        shape = RoundedCornerShape(OptionRadius),
        modifier = Modifier.fillMaxWidth().heightIn(min = OptionHeight),
        interaction = Modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = GriotSpacing.l, vertical = GriotSpacing.m + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m + 2.dp),
        ) {
            Box(
                modifier = Modifier.size(IconBox).background(fill, RoundedCornerShape(GriotRadius.row)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = tint)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = GriotTheme.type.button,
                    color = palette.ink,
                )
                Text(text = hint, style = GriotTheme.type.description, color = palette.muted)
            }
        }
    }
}
