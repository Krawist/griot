package com.griot.core.design.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val FieldHeight = 56.dp
private val BorderWidth = 1.5.dp
private val TextPadding = 18.dp

/**
 * Single-line text field of the forms, with its [label] above: a glass field with a magenta border while focused
 * and while it shows an [error], written under it.
 *
 * @param error What is wrong with the value, shown under the field; `null` when nothing is.
 * @param autoFocus Whether the field takes the focus, and opens the keyboard, when it first shows.
 */
@Composable
fun GriotTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    autoFocus: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val palette = GriotTheme.palette
    val motion = GriotTheme.motion
    val shape = RoundedCornerShape(GriotRadius.field)
    val textStyle = GriotTheme.type.field.copy(color = palette.ink)
    // The cursor goes at the end of a value set from outside, such as a title to rename.
    var field by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    if (field.text != value) field = TextFieldValue(value, TextRange(value.length))
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    if (autoFocus) LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val borderColor by animateColorAsState(
        targetValue = if (focused || error != null) palette.accent else palette.line,
        animationSpec = motion.colorChange(),
        label = "field border",
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
        Text(
            text = label,
            style = GriotTheme.type.label,
            color = palette.ink,
        )
        BasicTextField(
            value = field,
            onValueChange = {
                field = it
                if (it.text != value) onValueChange(it.text)
            },
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(palette.accent),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .height(FieldHeight)
                .focusRequester(focusRequester)
                .onFocusChanged { focused = it.isFocused }
                .background(palette.glass, shape)
                .border(BorderWidth, borderColor, shape)
                .semantics {
                    contentDescription = label
                    if (error != null) error(error)
                },
            decorationBox = { field ->
                Box(modifier = Modifier.padding(horizontal = TextPadding), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(text = placeholder, style = textStyle, color = palette.muted)
                    }
                    field()
                }
            },
        )
        // The last error stays drawn while it leaves.
        AnimatedPresence(value = error, enter = motion.itemEnter(), exit = motion.itemExit()) { message ->
            Text(text = message, style = GriotTheme.type.description, color = palette.accentText)
        }
    }
}
