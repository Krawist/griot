package com.griot.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.PreferredAi
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.SheetAction
import com.griot.core.design.theme.GriotTheme

/** The model the AI help asks first: the phone's or the online one; the other answers when it cannot. */
@Composable
internal fun PreferredAiSheet(preferred: PreferredAi, onSelect: (PreferredAi) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_ai_preferred),
            style = GriotTheme.type.sheetTitle,
            color = palette.ink,
        )
        PreferredAi.entries.forEach { option ->
            val selected = option == preferred
            SheetAction(
                icon = if (selected) DesignR.drawable.ic_check else DesignR.drawable.ic_circle,
                label = stringResource(option.labelRes()),
                onClick = { onSelect(option) },
                color = if (selected) palette.accentText else palette.ink,
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
