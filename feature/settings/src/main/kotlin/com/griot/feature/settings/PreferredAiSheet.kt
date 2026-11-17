package com.griot.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.PreferredAi
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.ChoiceCard
import com.griot.core.design.component.ChoiceGroup
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotTheme

/** The model the AI help asks first: the phone's or the online one, one card each; the other answers when it cannot. */
@Composable
internal fun PreferredAiSheet(preferred: PreferredAi, onSelect: (PreferredAi) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    val title = stringResource(R.string.settings_ai_preferred)
    GriotSheet(onDismiss = onDismiss) {
        Text(text = title, style = GriotTheme.type.sheetTitle, color = palette.ink)
        ChoiceGroup(label = title) {
            ChoiceCard(
                icon = DesignR.drawable.ic_phone,
                title = stringResource(R.string.settings_ai_preferred_device),
                hint = stringResource(R.string.settings_ai_device_ready),
                selected = preferred == PreferredAi.DEVICE,
                onClick = { onSelect(PreferredAi.DEVICE) },
            )
            ChoiceCard(
                icon = DesignR.drawable.ic_cloud,
                title = stringResource(R.string.settings_ai_preferred_online),
                hint = stringResource(R.string.settings_ai_online_ready),
                selected = preferred == PreferredAi.ONLINE,
                onClick = { onSelect(PreferredAi.ONLINE) },
            )
        }
        Text(
            text = stringResource(R.string.settings_ai_preferred_hint),
            style = GriotTheme.type.description,
            color = palette.muted,
        )
    }
}

/** The name of a preferred model. */
internal fun PreferredAi.labelRes(): Int = when (this) {
    PreferredAi.DEVICE -> R.string.settings_ai_preferred_device
    PreferredAi.ONLINE -> R.string.settings_ai_preferred_online
}
