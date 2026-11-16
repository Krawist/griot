package com.griot.feature.onboarding.importing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.onboarding.R

private val FieldHeight = 180.dp
private val FieldMaxHeight = 320.dp

/** A text copied from another app, pasted here and imported as one song, split into sections. */
@Composable
internal fun PasteTextSheet(onImport: (String) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.field)
    var text by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val style = MaterialTheme.typography.bodyLarge.copy(color = palette.ink)
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.import_paste_title),
            style = MaterialTheme.typography.titleMedium,
            color = palette.ink,
        )
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = style,
            cursorBrush = SolidColor(palette.accent),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = FieldHeight, max = FieldMaxHeight)
                .background(palette.glass, shape)
                .border(1.dp, palette.line, shape)
                .padding(GriotSpacing.l)
                .focusRequester(focus),
            decorationBox = { inner ->
                Box {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.import_paste_placeholder),
                            style = style,
                            color = palette.muted,
                        )
                    }
                    inner()
                }
            },
        )
        GriotButton(text = stringResource(R.string.import_paste_confirm), onClick = { onImport(text) })
    }
}
