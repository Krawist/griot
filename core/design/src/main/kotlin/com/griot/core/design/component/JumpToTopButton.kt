package com.griot.core.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.griot.core.design.R
import com.griot.core.design.theme.GriotTheme
import io.github.krawist.design.glass.GlassSurface

private val ButtonSize = 48.dp
private val ButtonElevation = 10.dp

/**
 * A round glass button that takes a scrolled screen back to its top, shown while its [ScrollControls] are hidden.
 *
 * @param label What it does, for accessibility: « Haut de la liste », « Haut de la chanson ».
 */
@Composable
fun JumpToTopButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassSurface(
        shape = CircleShape,
        strong = true,
        elevation = ButtonElevation,
        modifier = modifier.size(ButtonSize),
        interaction = Modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ButtonSize)) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_up),
                contentDescription = label,
                tint = GriotTheme.palette.ink,
            )
        }
    }
}
