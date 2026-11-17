package com.griot.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.PreferredAi
import com.griot.core.design.component.ChoiceOption
import com.griot.core.design.component.ChoiceSheet

/** The model the AI help asks first: the phone's or the online one; the other answers when it cannot. */
@Composable
internal fun PreferredAiSheet(preferred: PreferredAi, onSelect: (PreferredAi) -> Unit, onDismiss: () -> Unit) {
    ChoiceSheet(
        title = stringResource(R.string.settings_ai_preferred),
        description = stringResource(R.string.settings_ai_preferred_hint),
        options = listOf(
            ChoiceOption(
                value = PreferredAi.DEVICE,
                title = stringResource(R.string.settings_ai_preferred_device),
                hint = stringResource(R.string.settings_ai_device_ready),
            ),
            ChoiceOption(
                value = PreferredAi.ONLINE,
                title = stringResource(R.string.settings_ai_preferred_online),
                hint = stringResource(R.string.settings_ai_online_ready),
            ),
        ),
        selected = preferred,
        onApply = onSelect,
        onDismiss = onDismiss,
    )
}

/** The name of a preferred model. */
internal fun PreferredAi.labelRes(): Int = when (this) {
    PreferredAi.DEVICE -> R.string.settings_ai_preferred_device
    PreferredAi.ONLINE -> R.string.settings_ai_preferred_online
}
