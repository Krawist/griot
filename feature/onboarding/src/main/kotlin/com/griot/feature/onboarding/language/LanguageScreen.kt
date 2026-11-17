package com.griot.feature.onboarding.language

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.AppLanguage
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotButton
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.onboarding.OnboardingStep
import com.griot.feature.onboarding.R

private val OptionHeight = 76.dp
private val OptionRadius = 20.dp
private val CodeSize = 44.dp
private val RadioSize = 26.dp
private val RadioRing = 2.dp

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
        Column(
            modifier = Modifier.selectableGroup().semantics { contentDescription = groupLabel },
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.m),
        ) {
            LanguageOption(
                code = stringResource(R.string.language_french_code),
                name = stringResource(R.string.language_french_name),
                hint = stringResource(R.string.language_french_hint),
                selected = uiState.selected == AppLanguage.FRENCH,
                onClick = { onLanguageSelected(AppLanguage.FRENCH) },
            )
            LanguageOption(
                code = stringResource(R.string.language_english_code),
                name = stringResource(R.string.language_english_name),
                hint = stringResource(R.string.language_english_hint),
                selected = uiState.selected == AppLanguage.ENGLISH,
                onClick = { onLanguageSelected(AppLanguage.ENGLISH) },
            )
        }
        MixedTextsNote(modifier = Modifier.padding(top = GriotSpacing.xl))
    }
}

// Each language is named in itself, so the user finds theirs whatever the interface shows.
@Composable
private fun LanguageOption(code: String, name: String, hint: String, selected: Boolean, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val motion = GriotTheme.motion
    val shape = RoundedCornerShape(OptionRadius)
    val border by animateColorAsState(
        targetValue = if (selected) palette.accent else palette.line,
        animationSpec = motion.colorChange(),
        label = "option border",
    )
    val fill by animateColorAsState(
        targetValue = if (selected) palette.glassSelected else palette.glass,
        animationSpec = motion.colorChange(),
        label = "option fill",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = OptionHeight)
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .background(fill, shape)
            .border(width = if (selected) 2.dp else 1.dp, color = border, shape = shape)
            .padding(horizontal = GriotSpacing.l, vertical = GriotSpacing.m + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m + 2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(CodeSize)
                .background(palette.chip, RoundedCornerShape(GriotRadius.row))
                .border(1.dp, palette.line, RoundedCornerShape(GriotRadius.row)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = code, style = GriotTheme.type.code, color = palette.ink)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = name,
                style = GriotTheme.type.button,
                color = palette.ink,
            )
            Text(text = hint, style = GriotTheme.type.description, color = palette.muted)
        }
        Radio(selected = selected)
    }
}

@Composable
private fun Radio(selected: Boolean) {
    val palette = GriotTheme.palette
    val fill by animateColorAsState(
        targetValue = if (selected) palette.accentFill else palette.accentFill.copy(alpha = 0f),
        animationSpec = GriotTheme.motion.colorChange(),
        label = "radio",
    )
    Box(
        modifier = Modifier
            .size(RadioSize)
            .background(fill, CircleShape)
            .then(if (selected) Modifier else Modifier.border(RadioRing, palette.radioOff, CircleShape)),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_check),
                contentDescription = null,
                tint = palette.onAccent,
            )
        }
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
