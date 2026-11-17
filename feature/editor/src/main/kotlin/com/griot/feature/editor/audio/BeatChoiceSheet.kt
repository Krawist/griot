package com.griot.feature.editor.audio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.Attachment
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.R

private val RowHeight = 56.dp

/**
 * « Beat sous les paroles »: the song's files, the one playing checked; a tap plays another under the lyrics, the
 * trash removes a file from the song (it stays on the phone).
 */
@Composable
internal fun BeatChoiceSheet(
    files: List<Attachment>,
    current: String?,
    onChoose: (String) -> Unit,
    onDetach: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = GriotTheme.palette
    GriotSheet(onDismiss = onDismiss) {
        Text(text = stringResource(R.string.beat_choice_title), style = GriotTheme.type.sheetTitle, color = palette.ink)
        files.forEach { file ->
            val chosen = file.id == current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = RowHeight)
                    .clickable(role = Role.RadioButton, onClick = { onChoose(file.id) }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
            ) {
                Icon(
                    painter = painterResource(if (chosen) DesignR.drawable.ic_check else DesignR.drawable.ic_circle),
                    contentDescription = null,
                    tint = if (chosen) palette.accentText else palette.muted,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.title,
                        style = GriotTheme.type.bodyStrong,
                        color = palette.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    file.durationMillis?.let {
                        Text(text = formatDuration(it), style = GriotTheme.type.caption, color = palette.muted)
                    }
                }
                IconButton(onClick = { onDetach(file.id) }, modifier = Modifier.size(GriotSpacing.touchTarget)) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_trash),
                        contentDescription = stringResource(R.string.audios_delete),
                        tint = palette.muted,
                    )
                }
            }
        }
    }
}
