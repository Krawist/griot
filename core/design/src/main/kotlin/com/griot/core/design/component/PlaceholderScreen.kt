package com.griot.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotTheme

/** Temporary screen, used until a feature has its real screens. The first action is the main one. */
@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actions: List<PlaceholderAction> = emptyList(),
) {
    Surface(modifier = modifier.fillMaxSize(), color = GriotTheme.palette.background) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = GriotTheme.palette.muted,
            )
            actions.forEachIndexed { index, action ->
                if (index == 0) {
                    Button(onClick = action.onClick) { Text(action.label) }
                } else {
                    OutlinedButton(onClick = action.onClick) { Text(action.label) }
                }
            }
        }
    }
}
