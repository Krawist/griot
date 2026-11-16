package com.griot.core.design.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val ButtonHeight = 56.dp

/** Main action of a screen: a full-width magenta pill, such as « Commencer » or « Continuer ». */
@Composable
fun GriotButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, shape = CircleShape, modifier = modifier.fillMaxWidth().height(ButtonHeight)) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
