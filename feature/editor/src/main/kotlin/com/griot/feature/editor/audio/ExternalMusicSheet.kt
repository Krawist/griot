package com.griot.feature.editor.audio

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.R

/**
 * « Contrôler la musique des autres apps »: what the notification access adds, « Autoriser dans les réglages » (the
 * system setting) and « Plus tard » (play and pause only). Either way it is [onDone].
 */
@Composable
internal fun ExternalMusicSheet(onDone: () -> Unit) {
    val palette = GriotTheme.palette
    val context = LocalContext.current
    GriotSheet(onDismiss = onDone) {
        Text(
            text = stringResource(R.string.external_sheet_title),
            style = GriotTheme.type.sheetTitle,
            color = palette.ink,
        )
        Text(text = stringResource(R.string.external_sheet_body), style = GriotTheme.type.body, color = palette.ink2)
        Perks()
        Text(
            text = stringResource(R.string.external_sheet_note),
            style = GriotTheme.type.caption,
            color = palette.muted,
        )
        GriotButton(
            text = stringResource(R.string.external_allow),
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
                onDone()
            },
        )
        Text(
            text = stringResource(R.string.external_later),
            style = GriotTheme.type.textButton,
            color = palette.ink2,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = GriotSpacing.touchTarget)
                .clip(RoundedCornerShape(GriotSpacing.s))
                .clickable(role = Role.Button, onClick = onDone)
                .padding(vertical = GriotSpacing.m),
        )
    }
}

// What the access adds: the title and progress, the 15 seconds back, the pause.
@Composable
private fun Perks() {
    val palette = GriotTheme.palette
    Column(
        modifier = Modifier.padding(vertical = GriotSpacing.s),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s + 2.dp),
    ) {
        listOf(R.string.external_perk_title, R.string.external_perk_rewind, R.string.external_perk_pause).forEach {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
            ) {
                Box(
                    modifier = Modifier.size(24.dp).background(palette.syncSoft, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_check),
                        contentDescription = null,
                        tint = palette.syncText,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Text(text = stringResource(it), style = GriotTheme.type.body, color = palette.ink)
            }
        }
    }
}
