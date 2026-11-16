package com.griot.feature.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import com.griot.core.design.theme.GriotTheme

/**
 * A text of the song, edited in place and saved as typed. What is typed stays in the field: the saved [text] comes
 * back only while the field is not focused (a change from another device), so saving never moves the cursor. A line
 * break is not kept: a verse or a title is one line.
 */
@Composable
internal fun SyncedTextField(
    text: String,
    onTextChange: (String) -> Unit,
    style: TextStyle,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val palette = GriotTheme.palette
    var field by remember { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }
    var focused by remember { mutableStateOf(false) }
    if (!focused && field.text != text) field = TextFieldValue(text, TextRange(text.length))
    BasicTextField(
        value = field,
        onValueChange = { typed ->
            val line = typed.copy(text = typed.text.replace('\n', ' '))
            val changed = line.text != field.text
            field = line
            if (changed) onTextChange(line.text)
        },
        textStyle = style.copy(color = palette.ink),
        cursorBrush = SolidColor(palette.accent),
        modifier = modifier.onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Box {
                if (field.text.isEmpty()) Text(text = placeholder, style = style, color = palette.muted)
                inner()
            }
        },
    )
}
