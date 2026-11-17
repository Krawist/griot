package com.griot.feature.onboarding.importing

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.ImportResult
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotButton
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.onboarding.ONBOARDING_STEPS
import com.griot.feature.onboarding.OnboardingStep
import com.griot.feature.onboarding.R

// Text files: plain text and Markdown, which some file managers give no text type.
private val TextTypes = arrayOf("text/*", "application/octet-stream")

/** Step 4 of the onboarding: texts written elsewhere become songs, from Samsung Notes, files or a paste. */
@Composable
internal fun ImportScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    inOnboarding: Boolean = true,
    viewModel: ImportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sheet by rememberSaveable { mutableStateOf<ImportSheet?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        viewModel.onFilesPicked(uris.map { it.toString() })
    }
    ImportContent(
        uiState = uiState,
        step = ONBOARDING_STEPS.takeIf { inOnboarding },
        onBack = onBack,
        onDone = onDone,
        onSamsungNotes = { sheet = ImportSheet.SAMSUNG_NOTES },
        onFiles = { picker.launch(TextTypes) },
        onPaste = { sheet = ImportSheet.PASTE },
    )
    when (sheet) {
        ImportSheet.SAMSUNG_NOTES -> SamsungNotesHelpSheet(
            onPickFiles = {
                sheet = null
                picker.launch(TextTypes)
            },
            onDismiss = { sheet = null },
        )
        ImportSheet.PASTE -> PasteTextSheet(
            onImport = { text ->
                viewModel.onPaste(text)
                sheet = null
            },
            onDismiss = { sheet = null },
        )
        null -> Unit
    }
}

@Suppress("LongParameterList") // One callback per way to import, and the step's own.
@Composable
private fun ImportContent(
    uiState: ImportUiState,
    step: Int?,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onSamsungNotes: () -> Unit,
    onFiles: () -> Unit,
    onPaste: () -> Unit,
) {
    val palette = GriotTheme.palette
    OnboardingStep(
        step = step,
        onBack = onBack,
        title = stringResource(R.string.import_title),
        body = stringResource(R.string.import_body),
        actions = {
            // Opened from the settings, the import has no later and closes on « Terminé ».
            if (step != null) {
                Text(
                    text = stringResource(R.string.import_later),
                    style = GriotTheme.type.description,
                    color = palette.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            GriotButton(
                text = stringResource(if (step != null) R.string.import_done else R.string.import_close),
                onClick = onDone,
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.m)) {
            ImportOption(
                icon = DesignR.drawable.ic_note,
                tint = palette.accentText,
                fill = palette.accentSoft,
                title = stringResource(R.string.import_samsung_title),
                hint = stringResource(R.string.import_samsung_hint),
                onClick = onSamsungNotes,
            )
            ImportOption(
                icon = DesignR.drawable.ic_file,
                tint = palette.ink,
                fill = palette.yellow.copy(alpha = YELLOW_FILL),
                title = stringResource(R.string.import_files_title),
                hint = stringResource(R.string.import_files_hint),
                onClick = onFiles,
            )
            ImportOption(
                icon = DesignR.drawable.ic_paste,
                tint = palette.syncText,
                fill = palette.syncSoft,
                title = stringResource(R.string.import_paste_option),
                hint = stringResource(R.string.import_paste_hint),
                onClick = onPaste,
            )
            uiState.result?.let { ImportSummary(result = it) }
        }
    }
}

// « 3 chansons importées · 12 sections », then the files that were empty or unreadable.
@Composable
private fun ImportSummary(result: ImportResult) {
    val palette = GriotTheme.palette
    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.xs)) {
        Text(
            text = stringResource(
                R.string.import_summary,
                pluralStringResource(R.plurals.import_songs, result.songs, result.songs),
                pluralStringResource(R.plurals.import_sections, result.sections, result.sections),
            ),
            style = GriotTheme.type.body,
            color = palette.syncText,
        )
        result.failed.forEach { name ->
            Text(
                text = stringResource(R.string.import_failed, name),
                style = GriotTheme.type.description,
                color = palette.accentText,
            )
        }
    }
}

// The sun yellow behind the files icon, light enough for the dark icon on it.
private const val YELLOW_FILL = 0.35f

@Preview(name = "Light", heightDp = 844, widthDp = 390)
@Preview(name = "Dark", heightDp = 844, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ImportScreenPreview() {
    GriotTheme {
        ImportContent(
            uiState = ImportUiState(result = ImportResult(songs = 3, sections = 12, failed = listOf("Brouillon vide"))),
            step = ONBOARDING_STEPS,
            onBack = {},
            onDone = {},
            onSamsungNotes = {},
            onFiles = {},
            onPaste = {},
        )
    }
}
