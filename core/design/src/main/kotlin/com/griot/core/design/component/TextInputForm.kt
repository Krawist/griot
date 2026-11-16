package com.griot.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.griot.core.design.theme.GriotSpacing

/**
 * A field to type a name, such as a song's title, and its [confirmLabel] button, in a sheet. The field takes the
 * focus at once, and lets go of it on confirm, before the sheet closes: otherwise the focus would land in the screen
 * behind and open the keyboard there.
 *
 * @param value The name at first, the cursor at its end.
 */
@Composable
fun TextInputForm(
    value: String,
    label: String,
    placeholder: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(value) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val confirm = {
        focusManager.clearFocus()
        keyboard?.hide()
        onConfirm(text)
    }
    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.l)) {
        GriotTextField(
            value = text,
            onValueChange = { text = it },
            label = label,
            placeholder = placeholder,
            autoFocus = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { confirm() }),
        )
        GriotButton(text = confirmLabel, onClick = confirm)
    }
}
