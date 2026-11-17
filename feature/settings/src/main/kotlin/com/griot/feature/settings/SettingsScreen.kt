package com.griot.feature.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.ai.AiStatus
import com.griot.core.ai.online.KeyCheck
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.FileShareSheet
import com.griot.core.design.component.GriotSwitch
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

// The groups scroll clear of the bottom bar.
private val ListBottom = 112.dp

/** Settings: profile, sync, AI, texts, appearance and licences, in grouped glass cards. */
@Composable
internal fun SettingsScreen(
    onOpenImport: () -> Unit,
    onOpenTrash: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.onResume()
        onPauseOrDispose {}
    }
    var sheet by rememberSaveable { mutableStateOf<SettingsSheet?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(start = GriotSpacing.xl, top = GriotSpacing.l, end = GriotSpacing.xl, bottom = ListBottom),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.m + 2.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = GriotTheme.type.screenTitle,
            color = GriotTheme.palette.ink,
            modifier = Modifier.padding(horizontal = GriotSpacing.xs),
        )
        ProfileGroup(uiState = uiState, onSheet = { sheet = it })
        SyncGroup()
        AiGroup(
            uiState = uiState,
            onOnDeviceAiChange = viewModel::onOnDeviceAiChange,
            onOnlineAiChange = viewModel::onOnlineAiChange,
            onSheet = { sheet = it },
        )
        MusicGroup(controlled = uiState.externalMusic)
        TextsGroup(onImport = onOpenImport, onExport = viewModel::onExportAll, onTrash = onOpenTrash)
        AppearanceGroup(fontSize = uiState.fontSize, onLicences = { sheet = SettingsSheet.LICENCES })
    }
    SettingsSheets(uiState = uiState, sheet = sheet, viewModel = viewModel, onClose = { sheet = null })
}

@Composable
private fun ProfileGroup(uiState: SettingsUiState, onSheet: (SettingsSheet) -> Unit) {
    SettingsGroup(title = stringResource(R.string.settings_profile)) {
        SettingsRow(
            letter = uiState.authorName.take(1).uppercase().ifEmpty { stringResource(R.string.settings_name_letter) },
            tone = SettingsTone.BRAND,
            label = stringResource(R.string.settings_name),
            sub = stringResource(R.string.settings_name_sub),
            first = true,
            onClick = { onSheet(SettingsSheet.NAME) },
        ) { SettingsValue(value = uiState.authorName) }
        SettingsRow(
            letter = stringResource(R.string.settings_language_letter),
            tone = SettingsTone.BRAND,
            label = stringResource(R.string.settings_language),
            sub = stringResource(R.string.settings_language_sub),
            first = false,
            onClick = { onSheet(SettingsSheet.LANGUAGE) },
        ) { SettingsValue(value = stringResource(uiState.language.labelRes())) }
    }
}

// Google Drive comes with GRI-27: shown, not available yet.
@Composable
private fun SyncGroup() {
    val unavailable = stringResource(R.string.settings_unavailable)
    SettingsGroup(title = stringResource(R.string.settings_sync)) {
        SettingsRow(
            letter = stringResource(R.string.settings_drive_letter),
            tone = SettingsTone.SYNC,
            label = stringResource(R.string.settings_drive),
            sub = unavailable,
            first = true,
            enabled = false,
        ) {
            SettingsButton(
                label = stringResource(R.string.settings_sync_now),
                tone = SettingsTone.SYNC,
                enabled = false,
                onClick = {},
            )
        }
        SettingsRow(
            letter = stringResource(R.string.settings_auto_sync_letter),
            tone = SettingsTone.SYNC,
            label = stringResource(R.string.settings_auto_sync),
            sub = stringResource(R.string.settings_auto_sync_sub),
            first = false,
            enabled = false,
        ) { GriotSwitch(checked = false, onCheckedChange = {}, enabled = false) }
    }
}

// « Contrôle des autres apps »: its status, and the phone's notification access setting on a tap.
@Composable
private fun MusicGroup(controlled: Boolean) {
    val context = LocalContext.current
    SettingsGroup(title = stringResource(R.string.settings_music)) {
        SettingsRow(
            letter = "",
            icon = DesignR.drawable.ic_nav_songs,
            tone = SettingsTone.SYNC,
            label = stringResource(R.string.settings_music_control),
            sub = stringResource(R.string.settings_music_control_sub),
            first = true,
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            },
        ) {
            SettingsValue(
                value = stringResource(if (controlled) R.string.settings_music_on else R.string.settings_music_off),
            )
        }
    }
}

// The model asked first; Gemini Nano on the phone, when the phone can have it; Claude online, once the author's key
// is saved.
@Composable
private fun AiGroup(
    uiState: SettingsUiState,
    onOnDeviceAiChange: (Boolean) -> Unit,
    onOnlineAiChange: (Boolean) -> Unit,
    onSheet: (SettingsSheet) -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.settings_ai)) {
        SettingsRow(
            letter = "",
            icon = DesignR.drawable.ic_ai,
            tone = SettingsTone.AI,
            label = stringResource(R.string.settings_ai_preferred),
            sub = stringResource(R.string.settings_ai_preferred_sub),
            first = true,
            enabled = uiState.usableAi.size > 1,
            onClick = { onSheet(SettingsSheet.PREFERRED_AI) },
        ) {
            // The preferred one among two, else the only one, else none.
            val shown = if (uiState.usableAi.size > 1) uiState.preferredAi else uiState.usableAi.firstOrNull()
            SettingsValue(
                value = stringResource(shown?.labelRes() ?: R.string.settings_ai_preferred_none),
                chevron = uiState.usableAi.size > 1,
            )
        }
        DeviceAiRow(uiState = uiState, onChange = onOnDeviceAiChange)
        SettingsRow(
            letter = "",
            icon = DesignR.drawable.ic_cloud,
            tone = SettingsTone.AI,
            label = stringResource(R.string.settings_ai_online),
            sub = stringResource(
                if (uiState.apiKeySet) R.string.settings_ai_online_ready else R.string.settings_ai_online_no_key,
            ),
            first = false,
            enabled = uiState.apiKeySet,
            onClick = { onOnlineAiChange(!uiState.onlineAi) },
        ) {
            GriotSwitch(
                checked = uiState.apiKeySet && uiState.onlineAi,
                onCheckedChange = onOnlineAiChange,
                enabled = uiState.apiKeySet,
            )
        }
        SettingsRow(
            letter = "",
            icon = DesignR.drawable.ic_key,
            tone = SettingsTone.AI,
            label = stringResource(R.string.settings_key),
            sub = stringResource(R.string.settings_key_sub),
            first = false,
            onClick = { onSheet(SettingsSheet.API_KEY) },
        ) {
            SettingsValue(
                value = stringResource(
                    if (uiState.apiKeySet) R.string.settings_key_saved else R.string.settings_key_add,
                ),
            )
        }
    }
}

// Gemini Nano: its status, and whether the AI help may use it; dimmed when the phone cannot have it.
@Composable
private fun DeviceAiRow(uiState: SettingsUiState, onChange: (Boolean) -> Unit) {
    val supported = uiState.onDeviceStatus != AiStatus.UNAVAILABLE
    SettingsRow(
        letter = stringResource(R.string.settings_ai_device_letter),
        tone = SettingsTone.AI,
        label = stringResource(R.string.settings_ai_device),
        sub = stringResource(
            when (uiState.onDeviceStatus) {
                AiStatus.READY -> R.string.settings_ai_device_ready
                AiStatus.PREPARING -> R.string.settings_ai_device_preparing
                AiStatus.UNAVAILABLE -> R.string.settings_ai_device_unsupported
            },
        ),
        first = false,
        enabled = supported,
        onClick = { onChange(!uiState.onDeviceAi) },
    ) {
        GriotSwitch(
            checked = supported && uiState.onDeviceAi,
            onCheckedChange = onChange,
            enabled = supported,
        )
    }
}

@Composable
private fun TextsGroup(onImport: () -> Unit, onExport: () -> Unit, onTrash: () -> Unit) {
    SettingsGroup(title = stringResource(R.string.settings_texts)) {
        SettingsRow(
            letter = "",
            icon = DesignR.drawable.ic_import,
            tone = SettingsTone.BRAND,
            label = stringResource(R.string.settings_import),
            sub = stringResource(R.string.settings_import_sub),
            first = true,
            onClick = onImport,
        ) { SettingsValue(value = "") }
        SettingsRow(
            letter = "",
            icon = DesignR.drawable.ic_export,
            tone = SettingsTone.BRAND,
            label = stringResource(R.string.settings_export_all),
            sub = stringResource(R.string.settings_export_sub),
            first = false,
            onClick = onExport,
        ) { SettingsValue(value = "") }
        SettingsRow(
            letter = "",
            icon = DesignR.drawable.ic_trash,
            tone = SettingsTone.BRAND,
            label = stringResource(R.string.settings_trash),
            sub = stringResource(R.string.settings_trash_sub),
            first = false,
            onClick = onTrash,
        ) { SettingsValue(value = "") }
    }
}

// The theme follows the phone; the lyrics size is set with « Aa » in the editor.
@Composable
private fun AppearanceGroup(fontSize: Int, onLicences: () -> Unit) {
    SettingsGroup(title = stringResource(R.string.settings_appearance)) {
        SettingsRow(
            letter = "",
            icon = DesignR.drawable.ic_theme,
            tone = SettingsTone.BRAND,
            label = stringResource(R.string.settings_theme),
            sub = stringResource(R.string.settings_theme_sub),
            first = true,
        )
        SettingsRow(
            letter = stringResource(R.string.settings_font_size_letter),
            tone = SettingsTone.BRAND,
            label = stringResource(R.string.settings_font_size),
            sub = stringResource(R.string.settings_font_size_sub),
            first = false,
        ) { SettingsValue(value = stringResource(R.string.settings_font_size_value, fontSize), chevron = false) }
        SettingsRow(
            letter = "",
            icon = DesignR.drawable.ic_licence,
            tone = SettingsTone.BRAND,
            label = stringResource(R.string.settings_licences),
            sub = stringResource(R.string.settings_licences_sub),
            first = false,
            onClick = onLicences,
        ) { SettingsValue(value = "") }
    }
}

// The open sheet, and the share sheet of an export.
@Composable
private fun SettingsSheets(
    uiState: SettingsUiState,
    sheet: SettingsSheet?,
    viewModel: SettingsViewModel,
    onClose: () -> Unit,
) {
    when (sheet) {
        SettingsSheet.NAME -> NameSheet(
            name = uiState.authorName,
            onConfirm = { name ->
                viewModel.onNameChange(name)
                onClose()
            },
            onDismiss = onClose,
        )
        SettingsSheet.LANGUAGE -> AppLanguageSheet(
            language = uiState.language,
            onSelect = { language ->
                onClose()
                viewModel.onLanguageSelected(language)
            },
            onDismiss = onClose,
        )
        SettingsSheet.LICENCES -> LicencesSheet(onDismiss = onClose)
        SettingsSheet.PREFERRED_AI -> PreferredAiSheet(
            preferred = uiState.preferredAi,
            onSelect = { preferred ->
                viewModel.onPreferredAiChange(preferred)
                onClose()
            },
            onDismiss = onClose,
        )
        SettingsSheet.API_KEY -> {
            val close = {
                viewModel.onApiKeyClosed()
                onClose()
            }
            // An accepted key closes the sheet.
            LaunchedEffect(uiState.keyCheck) { if (uiState.keyCheck == KeyCheck.ACCEPTED) close() }
            ApiKeySheet(
                uiState = uiState,
                onSave = viewModel::onApiKeySave,
                onClear = {
                    viewModel.onApiKeyClear()
                    close()
                },
                onDismiss = close,
            )
        }
        null -> Unit
    }
    uiState.export?.let { file ->
        FileShareSheet(
            name = file.name,
            path = file.path,
            mimeType = file.mimeType,
            onSave = viewModel::onSaveExport,
            onDismiss = viewModel::onExportClosed,
        )
    }
}
