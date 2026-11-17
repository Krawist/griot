package com.griot.feature.editor.ai

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griot.core.ai.CheckedSuggestion
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.core.design.theme.LyricTextStyle
import com.griot.feature.editor.R

private val ChipHeight = 40.dp
private const val USED_ALPHA = 0.55f

/** « Rimes »: the words of the dictionary that rhyme with the verse, by syllables; a tap inserts one. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RhymesTab(uiState: AiPanelUiState, onWord: (String) -> Unit) {
    val palette = GriotTheme.palette
    if (uiState.rhymes.isEmpty()) {
        if (!uiState.loading) Note(text = stringResource(R.string.ai_rhymes_empty))
        return
    }
    Note(text = stringResource(R.string.ai_rhymes_title, uiState.ending))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        uiState.rhymes.forEach { rhyme ->
            val shape = RoundedCornerShape(ChipHeight / 2)
            Row(
                modifier = Modifier
                    .height(ChipHeight)
                    .clip(shape)
                    .background(palette.glass, shape)
                    .border(1.dp, palette.line, shape)
                    .clickable(role = Role.Button, onClick = { onWord(rhyme.word) })
                    .alpha(if (rhyme.used) USED_ALPHA else 1f)
                    .padding(horizontal = GriotSpacing.m + 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = rhyme.word,
                    style = LyricTextStyle.copy(
                        fontSize = 16.sp,
                        textDecoration = if (rhyme.used) TextDecoration.LineThrough else null,
                    ),
                    color = palette.ink,
                )
                Text(
                    text = rhyme.syllables.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted,
                )
            }
        }
    }
    if (uiState.rhymes.any { it.used }) Note(text = stringResource(R.string.ai_rhymes_used), small = true)
}

/** A tab answered by a model: its items, or why there are none, with a retry after a failure. */
@Composable
internal fun <T> ModelTab(
    content: AiContent<T>,
    onRetry: () -> Unit,
    @StringRes empty: Int = R.string.ai_nothing_kept,
    item: @Composable (T) -> Unit,
) {
    when {
        content.loading -> {
            Note(text = stringResource(R.string.ai_thinking))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = GriotTheme.palette.ai,
                trackColor = GriotTheme.palette.aiSoft,
            )
        }
        content.unavailable -> Note(text = stringResource(R.string.ai_unavailable))
        content.failed -> Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) { Note(text = stringResource(R.string.ai_failed)) }
            TextAction(text = stringResource(R.string.ai_retry), onClick = onRetry)
        }
        content.answered && content.items.isEmpty() -> Note(text = stringResource(empty))
        else -> content.items.forEach { item(it) }
    }
}

/** A version of the verse proposed by a model, its count from the phonetic engine, and « Utiliser ». */
@Composable
internal fun SuggestionCard(suggestion: CheckedSuggestion, onUse: () -> Unit) {
    val palette = GriotTheme.palette
    Card {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GriotSpacing.xs)) {
            Text(text = suggestion.text, style = LyricTextStyle.copy(fontSize = 17.sp), color = palette.ink)
            Text(
                text = pluralStringResource(R.plurals.ai_syllables, suggestion.syllables, suggestion.syllables),
                style = MaterialTheme.typography.bodySmall,
                color = if (suggestion.guessed) palette.muted.copy(alpha = USED_ALPHA) else palette.muted,
            )
        }
        Box(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(palette.inverse)
                .clickable(role = Role.Button, onClick = onUse)
                .padding(horizontal = GriotSpacing.m + 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.ai_use),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.onInverse,
            )
        }
    }
}

/** A remark of a model, after an orange dot. */
@Composable
internal fun RemarkCard(remark: String) {
    val palette = GriotTheme.palette
    Card(verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.padding(top = 7.dp).size(8.dp).background(palette.ai, CircleShape))
        Text(text = remark, style = MaterialTheme.typography.bodyMedium, color = palette.ink2)
    }
}

@Composable
private fun Card(
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit,
) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.field + 2.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.glass, shape)
            .border(1.dp, palette.line, shape)
            .padding(horizontal = GriotSpacing.m + 2.dp, vertical = GriotSpacing.m),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
        content = content,
    )
}

@Composable
private fun Note(text: String, small: Boolean = false) {
    Text(
        text = text,
        style = if (small) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
        color = GriotTheme.palette.ink2,
    )
}
