package com.griot.core.design.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotTheme

private val ButtonHeight = 56.dp

/**
 * Main action of a screen: a full-width magenta pill, such as « Commencer » or « Continuer ».
 *
 * @param enabled Whether it can be tapped; a disabled button is drawn muted.
 */
@Composable
fun GriotButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = modifier.fillMaxWidth().height(ButtonHeight),
    ) {
        Text(text = text, style = GriotTheme.type.button)
    }
}
