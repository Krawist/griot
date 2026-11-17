package com.griot.feature.onboarding.language

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.AppLanguage
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.ChoiceCard
import com.griot.core.design.component.ChoiceGroup
import com.griot.core.design.component.GriotButton
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.onboarding.OnboardingStep
import com.griot.feature.onboarding.R

/** Step 2 of the onboarding: the language of the interface and of new songs. */
@Composable
internal fun LanguageScreen(onBack: () -> Unit, onNext: () -> Unit, viewModel: LanguageViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LanguageContent(
        uiState = uiState,
        onBack = onBack,
        onLanguageSelected = viewModel::onLanguageSelected,
        onContinue = { viewModel.onContinue(onSaved = onNext) },
    )
}

@Composable
private fun LanguageContent(
    uiState: LanguageUiState,
    onBack: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onContinue: () -> Unit,
) {
    val groupLabel = stringResource(R.string.language_group)
    OnboardingStep(
        step = 2,
        onBack = onBack,
        title = stringResource(R.string.language_title),
        body = stringResource(R.string.language_body),
        actions = { GriotButton(text = stringResource(R.string.onboarding_continue), onClick = onContinue) },
    ) {
        // Each language is named in itself, so the user finds theirs whatever the interface shows.
        ChoiceGroup(label = groupLabel) {
            ChoiceCard(
                badge = stringResource(R.string.language_french_code),
                title = stringResource(R.string.language_french_name),
                hint = stringResource(R.string.language_french_hint),
                selected = uiState.selected == AppLanguage.FRENCH,
                onClick = { onLanguageSelected(AppLanguage.FRENCH) },
            )
            ChoiceCard(
                badge = stringResource(R.string.language_english_code),
                title = stringResource(R.string.language_english_name),
                hint = stringResource(R.string.language_english_hint),
                selected = uiState.selected == AppLanguage.ENGLISH,
                onClick = { onLanguageSelected(AppLanguage.ENGLISH) },
            )
        }
        MixedTextsNote(modifier = Modifier.padding(top = GriotSpacing.xl))
    }
}

@Composable
private fun MixedTextsNote(modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.infoSoft, RoundedCornerShape(GriotRadius.field))
            .padding(horizontal = GriotSpacing.l, vertical = GriotSpacing.m + 2.dp),
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_info),
            contentDescription = null,
            tint = palette.infoText,
            modifier = Modifier.padding(top = 1.dp),
        )
        Text(
            text = stringResource(R.string.language_mixed),
            style = GriotTheme.type.description,
            color = palette.infoText,
        )
    }
}

@Preview(name = "Light", heightDp = 844, widthDp = 390)
@Preview(name = "Dark", heightDp = 844, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LanguageScreenPreview() {
    GriotTheme {
        LanguageContent(
            uiState = LanguageUiState(selected = AppLanguage.FRENCH),
            onBack = {},
            onLanguageSelected = {},
            onContinue = {},
        )
    }
}
