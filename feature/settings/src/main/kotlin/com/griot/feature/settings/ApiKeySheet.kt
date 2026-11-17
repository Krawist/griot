package com.griot.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import com.griot.core.ai.online.KeyCheck
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.GriotTextField
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/**
 * « Clé API Anthropic »: the author pastes a key, hidden behind dots; « Vérifier et enregistrer » asks Anthropic
 * first. A refused or unchecked key says so under the field. « Supprimer la clé » forgets the saved one.
 * The settings' [uiState] tell whether a key is saved and what Anthropic said of the last one.
 */
@Composable
internal fun ApiKeySheet(
    uiState: SettingsUiState,
    onSave: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = GriotTheme.palette
    val checking = uiState.checkingKey
    var key by rememberSaveable { mutableStateOf("") }
    val save = { if (!checking && key.isNotBlank()) onSave(key) }
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_key_title),
            style = GriotTheme.type.sheetTitle,
            color = palette.ink,
        )
        Text(text = stringResource(R.string.settings_key_body), style = GriotTheme.type.body, color = palette.ink2)
        GriotTextField(
            value = key,
            onValueChange = { key = it },
            label = stringResource(R.string.settings_key_label),
            placeholder = stringResource(R.string.settings_key_placeholder),
            error = when (uiState.keyCheck) {
                KeyCheck.REFUSED -> stringResource(R.string.settings_key_refused)
                KeyCheck.UNREACHABLE -> stringResource(R.string.settings_key_unreachable)
                KeyCheck.ACCEPTED, null -> null
            },
            autoFocus = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { save() }),
            visualTransformation = PasswordVisualTransformation(),
        )
        GriotButton(
            text = stringResource(if (checking) R.string.settings_key_checking else R.string.settings_key_save),
            onClick = save,
        )
        if (uiState.apiKeySet) {
            Text(
                text = stringResource(R.string.settings_key_clear),
                style = GriotTheme.type.textButton,
                color = palette.accentText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = GriotSpacing.touchTarget)
                    .clip(RoundedCornerShape(GriotSpacing.s))
                    .clickable(role = Role.Button, onClick = onClear)
                    .padding(vertical = GriotSpacing.m),
            )
        }
    }
}
