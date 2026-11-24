package com.griot.feature.ideas

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.design.format.text
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import io.github.krawist.design.glass.GlassSurface

// The sun yellow behind the « Libre » chip of a free idea.
private const val FREE_ALPHA = 0.22f

/**
 * One idea: its text or image, the song it is linked to (« Libre » otherwise), when it was saved, and « Rattacher »
 * or « Ouvrir ». A long press opens its actions.
 */
@Composable
internal fun IdeaCard(item: IdeaItem, onAction: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    val idea = item.idea
    val linked = idea.songId != null
    GlassSurface(
        shape = RoundedCornerShape(GriotRadius.card),
        modifier = modifier.fillMaxWidth(),
        interaction = Modifier.combinedClickable(onClick = onAction, onLongClick = onLongClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = GriotSpacing.l, vertical = GriotSpacing.m + 2.dp),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        ) {
            idea.imagePath?.let { IdeaImage(path = it) }
            if (idea.text.isNotBlank()) {
                Text(text = idea.text, style = GriotTheme.type.quote, color = palette.ink)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
            ) {
                val tag = if (linked) {
                    idea.songTitle?.ifBlank { null } ?: stringResource(R.string.ideas_untitled_song)
                } else {
                    stringResource(R.string.ideas_free)
                }
                Text(
                    text = tag,
                    style = GriotTheme.type.captionStrong,
                    color = if (linked) palette.accentText else palette.ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .background(
                            if (linked) palette.accentSoft else palette.yellow.copy(alpha = FREE_ALPHA),
                            RoundedCornerShape(GriotRadius.badge),
                        )
                        .padding(horizontal = GriotSpacing.s, vertical = 3.dp),
                )
                Text(
                    text = item.saved.text(),
                    style = GriotTheme.type.caption,
                    color = palette.muted,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = onAction,
                    colors = ButtonDefaults.textButtonColors(contentColor = palette.accentText),
                ) {
                    Text(
                        text = stringResource(if (linked) R.string.ideas_open else R.string.ideas_link),
                        style = GriotTheme.type.smallStrong,
                    )
                }
            }
        }
    }
}
