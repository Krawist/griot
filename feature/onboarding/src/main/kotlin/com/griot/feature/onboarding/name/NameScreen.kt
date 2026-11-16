package com.griot.feature.onboarding.name

import android.content.res.Configuration
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotTextField
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.onboarding.OnboardingStep
import com.griot.feature.onboarding.R

/** Step 1 of the onboarding: the first name or artist name that signs the exported texts. */
@Composable
internal fun NameScreen(onBack: () -> Unit, onNext: () -> Unit, viewModel: NameViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NameContent(
        uiState = uiState,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onContinue = { viewModel.onContinue(onSaved = onNext) },
    )
}

@Composable
private fun NameContent(
    uiState: NameUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    OnboardingStep(
        step = 1,
        onBack = onBack,
        title = stringResource(R.string.name_title),
        body = stringResource(R.string.name_body),
        actions = { GriotButton(text = stringResource(R.string.onboarding_continue), onClick = onContinue) },
    ) {
        GriotTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.name_label),
            placeholder = stringResource(R.string.name_placeholder),
            error = if (uiState.emptyNameShown) stringResource(R.string.name_empty) else null,
            autoFocus = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onContinue() }),
        )
    }
}

@Preview(name = "Light", heightDp = 844, widthDp = 390)
@Preview(name = "Dark", heightDp = 844, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NameScreenPreview() {
    GriotTheme {
        NameContent(uiState = NameUiState(emptyNameShown = true), onBack = {}, onNameChange = {}, onContinue = {})
    }
}
