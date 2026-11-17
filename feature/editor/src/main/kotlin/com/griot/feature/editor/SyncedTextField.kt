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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotTheme

/**
 * A text of the song, edited in place and saved as typed. What is typed stays in the field: the saved [text] comes
 * back only while the field is not focused (a change from another device), so saving never moves the cursor. The
 * text wraps on screen and stays one line.
 *
 * @param onLines Called instead of [onTextChange] when a line break is typed or pasted, with the lines and where the
 * cursor is in the last one; `null` turns line breaks into spaces, as in a title.
 * @param onBackspaceAtStart Called with the text when Backspace is pressed with the cursor at the very start.
 * @param highlight A part of the text drawn as a rhyme; `null` for none.
 * @param onFocused Called when the field takes the focus.
 * @param onCursor Called with the cursor's place after each change of the field.
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
    highlight: TextHighlight? = null,
    onFocused: () -> Unit = {},
    onCursor: (Int) -> Unit = {},
    focus: VerseFocus? = null,
    onFocusHandled: () -> Unit = {},
) {
    val palette = GriotTheme.palette
    var field by remember { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
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
            field =
                typed(typed = typed, field = field, onLines = onLines, onTextChange = onTextChange)
            onCursor(field.selection.start)
        },
        textStyle = style.copy(color = palette.ink),
        cursorBrush = SolidColor(palette.accent),
        visualTransformation = highlight?.let { RhymeBackground(it) } ?: VisualTransformation.None,
        onTextLayout = { layout = it },
        modifier = modifier
            .drawBehind { highlight?.let { drawUnderline(it, layout) } }
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused && !focused) onFocused()
                focused = it.isFocused
            }
            .onPreviewKeyEvent { event ->
                backspaceAtStart(event = event, field = field, onBackspaceAtStart = onBackspaceAtStart)
            },
        decorationBox = { inner ->
            Box {
                if (field.text.isEmpty()) Text(text = placeholder, style = style, color = palette.muted)
                inner()
            }
        },
    )
}

private val UnderlineWidth = 2.dp

// What the field shows after [typed]: a line break hands the lines over to [onLines], and the field keeps the first;
// otherwise the text, on one line, is saved when it changed.
private fun typed(
    typed: TextFieldValue,
    field: TextFieldValue,
    onLines: ((List<String>, Int) -> Unit)?,
    onTextChange: (String) -> Unit,
): TextFieldValue {
    if (onLines != null && '\n' in typed.text) {
        val lines = typed.text.split('\n')
        onLines(lines, typed.selection.start - typed.text.lastIndexOf('\n') - 1)
        return TextFieldValue(lines.first(), TextRange(lines.first().length))
    }
    val line = typed.copy(text = typed.text.replace('\n', ' '))
    if (line.text != field.text) onTextChange(line.text)
    return line
}

// Backspace with the cursor at the very start goes to [onBackspaceAtStart], which then handles the key.
private fun backspaceAtStart(event: KeyEvent, field: TextFieldValue, onBackspaceAtStart: ((String) -> Unit)?): Boolean {
    val atStart = field.selection.collapsed && field.selection.start == 0
    val backspace = event.type == KeyEventType.KeyDown && event.key == Key.Backspace
    if (onBackspaceAtStart == null || !backspace || !atStart) return false
    onBackspaceAtStart(field.text)
    return true
}

// The background of the rhyming letters; the text itself is unchanged, so offsets map one to one.
private class RhymeBackground(private val highlight: TextHighlight) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val start = highlight.range.first.coerceIn(0, text.length)
        val end = (highlight.range.last + 1).coerceIn(start, text.length)
        val styled = AnnotatedString.Builder(text).apply {
            addStyle(SpanStyle(background = highlight.background), start, end)
        }.toAnnotatedString()
        return TransformedText(styled, OffsetMapping.Identity)
    }

    override fun equals(other: Any?) = other is RhymeBackground && other.highlight == highlight

    override fun hashCode() = highlight.hashCode()
}

// The coloured line under the rhyming letters, on the line where they end.
private fun DrawScope.drawUnderline(highlight: TextHighlight, layout: TextLayoutResult?) {
    val text = layout ?: return
    val length = text.layoutInput.text.length
    if (length == 0) return
    val first = highlight.range.first.coerceIn(0, length - 1)
    val last = highlight.range.last.coerceIn(first, length - 1)
    val start = text.getBoundingBox(first)
    val end = text.getBoundingBox(last)
    val y = end.bottom - UnderlineWidth.toPx() / 2
    val left = if (text.getLineForOffset(first) == text.getLineForOffset(last)) start.left else 0f
    drawLine(
        color = highlight.underline,
        start = Offset(left, y),
        end = Offset(end.right, y),
        strokeWidth = UnderlineWidth.toPx(),
    )
}
