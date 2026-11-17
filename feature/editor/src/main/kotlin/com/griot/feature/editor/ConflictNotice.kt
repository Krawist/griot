package com.griot.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/**
 * Under the top bar, in the sync colour: verses changed on two devices kept both texts. « Voir » opens the history of
 * the first one, « Ignorer » hides the notice.
 *
 * @param count How many verses had a conflict.
 */
@Composable
internal fun ConflictNotice(count: Int, onShow: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.field)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.syncSoft, shape)
            .padding(start = GriotSpacing.l, end = GriotSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs),
    ) {
        Text(
            text = pluralStringResource(R.plurals.editor_conflicts, count, count),
            style = GriotTheme.type.description,
            color = palette.syncText,
            modifier = Modifier.weight(1f).padding(vertical = GriotSpacing.m),
        )
        NoticeButton(text = stringResource(R.string.editor_conflicts_dismiss), onClick = onDismiss)
        NoticeButton(text = stringResource(R.string.editor_conflicts_show), onClick = onShow)
    }
}

@Composable
private fun NoticeButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = GriotSpacing.touchTarget)
            .clip(RoundedCornerShape(GriotRadius.control))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = GriotSpacing.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = GriotTheme.type.smallStrong,
            color = GriotTheme.palette.syncText,
        )
    }
}
