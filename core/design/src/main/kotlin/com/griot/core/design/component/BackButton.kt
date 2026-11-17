package com.griot.core.design.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.griot.core.design.R
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

// The chevron sits in the middle of a 44 dp target: pulled back by this much, its stroke lines up with the column's
// edge, like the title and the cards below it. The target keeps its 44 dp.
private val ChevronInset = (-16).dp

/**
 * « Retour » at the start of a page's column: a 44 dp touch target whose chevron lines up with the column's edge.
 *
 * @param description What it goes back to, for screen readers (« Retour à l'accueil »).
 */
@Composable
fun BackButton(description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Pulled back in the layout too, so that what follows it in a row keeps its usual gap with the chevron.
    val pulled = modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val inset = ChevronInset.roundToPx()
        layout(width = placeable.width + inset, height = placeable.height) { placeable.place(x = inset, y = 0) }
    }
    IconButton(onClick = onClick, modifier = pulled.size(GriotSpacing.touchTarget)) {
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = description,
            tint = GriotTheme.palette.ink,
        )
    }
}
