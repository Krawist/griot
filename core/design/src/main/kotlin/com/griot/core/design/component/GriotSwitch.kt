package com.griot.core.design.component

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.griot.core.design.theme.GriotTheme

/** A switch in the turquoise of states; [enabled] false while its feature is not available. */
@Composable
fun GriotSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = GriotTheme.palette
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedTrackColor = palette.sync,
            checkedThumbColor = palette.onAccent,
            uncheckedTrackColor = palette.switchOff,
            uncheckedThumbColor = palette.onAccent,
            uncheckedBorderColor = palette.switchOff,
            disabledUncheckedTrackColor = palette.switchOff,
            disabledUncheckedThumbColor = palette.onAccent,
            disabledUncheckedBorderColor = palette.switchOff,
        ),
    )
}
