package com.griot.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.TextInputForm

/** Changes the author's name, which signs the exports. */
@Composable
internal fun NameSheet(name: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    GriotSheet(onDismiss = onDismiss) {
        TextInputForm(
            value = name,
            label = stringResource(R.string.settings_name),
            placeholder = stringResource(R.string.settings_name_placeholder),
            confirmLabel = stringResource(R.string.settings_save),
            onConfirm = onConfirm,
        )
    }
}
