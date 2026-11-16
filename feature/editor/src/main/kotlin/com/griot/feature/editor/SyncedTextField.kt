package com.griot.feature.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import com.griot.core.design.theme.GriotTheme

/**
 * A text of the song, edited in place and saved as typed. What is typed stays in the field: the saved [text] comes
 * back only while the field is not focused (a change from another device), so saving never moves the cursor. The
 * text wraps on screen and stays one line.
 *
 * @param onLines Called instead of [onTextChange] when a line break is typed or pasted, with the lines and where the
 * cursor is in the last one; `null` turns line breaks into spaces, as in a title.
 * @param onBackspaceAtStart Called with the text when Backspace is pressed with the cursor at the very start.
 * @param focus When set, the field shows its text with the cursor at its place, takes the focus, then calls
 * [onFocusHandled].
 */
@Suppress("LongParameterList") // The text, its style and one callback per way of typing.
@Composable
internal fun SyncedTextField(
    text: String,
    onTextChange: (String) -> Unit,
    style: TextStyle,
    placeholder: String,
    modifier: Modifier = Modifier,
    onLines: ((List<String>, Int) -> Unit)? = null,
    onBackspaceAtStart: ((String) -> Unit)? = null,
    focus: VerseFocus? = null,
    onFocusHandled: () -> Unit = {},
) {
    val palette = GriotTheme.palette
    var field by remember { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    if (!focused && field.text != text) field = TextFieldValue(text, TextRange(text.length))
    LaunchedEffect(focus) {
        if (focus != null) {
            field = TextFieldValue(focus.text, TextRange(focus.cursor))
            focusRequester.requestFocus()
            onFocusHandled()
        }
    }
    BasicTextField(
        value = field,
        onValueChange = { typed ->
            if (onLines != null && '\n' in typed.text) {
                val lines = typed.text.split('\n')
                // The field keeps the first line; the others go to the verses below.
                field = TextFieldValue(lines.first(), TextRange(lines.first().length))
                onLines(lines, typed.selection.start - typed.text.lastIndexOf('\n') - 1)
            } else {
                val line = typed.copy(text = typed.text.replace('\n', ' '))
                val changed = line.text != field.text
                field = line
                if (changed) onTextChange(line.text)
            }
        },
        textStyle = style.copy(color = palette.ink),
        cursorBrush = SolidColor(palette.accent),
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { event ->
                val atStart = field.selection.collapsed && field.selection.start == 0
                val backspace = event.type == KeyEventType.KeyDown && event.key == Key.Backspace
                if (onBackspaceAtStart != null && backspace && atStart) {
                    onBackspaceAtStart(field.text)
                    true
                } else {
                    false
                }
            },
        decorationBox = { inner ->
            Box {
                if (field.text.isEmpty()) Text(text = placeholder, style = style, color = palette.muted)
                inner()
            }
        },
    )
}
