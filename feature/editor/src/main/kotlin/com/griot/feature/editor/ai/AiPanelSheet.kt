package com.griot.feature.editor.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.ai.AiLocation
import com.griot.core.data.model.SongLanguage
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.core.design.theme.LyricTextStyle
import com.griot.feature.editor.R
import com.griot.feature.editor.text

private val TabHeight = 40.dp
private val ContentMaxHeight = 380.dp

/**
 * The AI panel of the verse [verseId]: the verse, its place and count at the top, then « Rimes », « Reformuler » and
 * « Critiquer », and where the answers come from at the bottom. Orange marks every AI element.
 *
 * @param language The language the song is read in, French or English.
 * @param onInsertWord Inserts a rhyme at the cursor of the verse.
 * @param onUseSuggestion Replaces the verse by a suggestion.
 */
@Suppress("LongParameterList") // The verse, and one callback per action on it.
@Composable
internal fun AiPanelSheet(
    songId: String,
    verseId: String,
    language: SongLanguage,
    onInsertWord: (String) -> Unit,
    onUseSuggestion: (String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: AiPanelViewModel = hiltViewModel<AiPanelViewModel, AiPanelViewModel.Factory>(
        key = "$verseId/$language",
        creationCallback = { factory -> factory.create(songId, verseId, language) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GriotSheet(onDismiss = onDismiss) {
        PanelHeader(uiState = uiState, onClose = onDismiss)
        PanelTabs(selected = uiState.tab, onSelect = viewModel::onTabSelected)
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(max = ContentMaxHeight).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp),
        ) {
            when (uiState.tab) {
                AiTab.RHYMES -> RhymesTab(uiState = uiState, onWord = onInsertWord)
                AiTab.REPHRASE -> ModelTab(
                    content = uiState.rephrase,
                    preparing = uiState.preparing,
                    onRetry = viewModel::onMoreIdeas,
                ) { suggestion ->
                    SuggestionCard(
                        suggestion = suggestion,
                        verse = uiState.verse,
                        syllables = uiState.syllables,
                        onUse = { onUseSuggestion(suggestion.text) },
                    )
                }
                AiTab.CRITIQUE -> ModelTab(
                    content = uiState.critique,
                    preparing = uiState.preparing,
                    onRetry = viewModel::onMoreIdeas,
                    empty = R.string.ai_critique_empty,
                ) { remark -> RemarkCard(remark = remark) }
            }
        }
        PanelFooter(
            tab = uiState.tab,
            location = uiState.location,
            preparing = uiState.preparing,
            onMore = viewModel::onMoreIdeas,
        )
    }
}

// « COUPLET 1 · VERS 2 · 9 SYLLABES », the verse in quotes, and the close button.
@Composable
private fun PanelHeader(uiState: AiPanelUiState, onClose: () -> Unit) {
    val palette = GriotTheme.palette
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            uiState.section?.let { section ->
                val place = stringResource(R.string.ai_place, section.text(), uiState.number)
                val count = pluralStringResource(R.plurals.ai_syllables, uiState.syllables, uiState.syllables)
                Text(
                    text = "$place · $count".uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp,
                    ),
                    color = palette.aiText,
                )
            }
            Text(
                text = stringResource(R.string.ai_quoted, uiState.verse),
                style = LyricTextStyle.copy(fontSize = 19.sp),
                color = palette.ink,
            )
        }
        IconButton(onClick = onClose, modifier = Modifier.size(GriotSpacing.touchTarget)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_close),
                contentDescription = stringResource(R.string.ai_close),
                tint = palette.ink2,
            )
        }
    }
}

@Composable
private fun PanelTabs(selected: AiTab, onSelect: (AiTab) -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.field + 2.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.glass, shape)
            .border(1.dp, palette.line, shape)
            .padding(GriotSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs),
    ) {
        AiTab.entries.forEach { tab ->
            val on = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(TabHeight)
                    .clip(RoundedCornerShape(GriotRadius.row))
                    .background(if (on) palette.ai else palette.glass.copy(alpha = 0f))
                    .clickable(role = Role.Tab, onClick = { onSelect(tab) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(tab.labelRes()),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (on) palette.onAi else palette.ink2,
                )
            }
        }
    }
}

// Where the answers come from, and « Autres idées » on the model tabs.
@Composable
private fun PanelFooter(tab: AiTab, location: AiLocation?, preparing: Boolean, onMore: () -> Unit) {
    val palette = GriotTheme.palette
    val source = when {
        tab == AiTab.RHYMES -> R.string.ai_source_dictionary
        location == AiLocation.DEVICE -> R.string.ai_source_device
        location == AiLocation.ONLINE -> R.string.ai_source_online
        preparing -> R.string.ai_source_preparing
        else -> R.string.ai_source_none
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_ai),
            contentDescription = null,
            tint = palette.muted,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(source),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
            modifier = Modifier.weight(1f),
        )
        if (tab != AiTab.RHYMES && location != null) {
            TextAction(text = stringResource(R.string.ai_more), onClick = onMore)
        }
    }
}

/** A text button in the AI colour. */
@Composable
internal fun TextAction(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = GriotSpacing.touchTarget)
            .clip(RoundedCornerShape(GriotRadius.control))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = GriotSpacing.m),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = GriotTheme.palette.aiText,
        )
    }
}

private fun AiTab.labelRes(): Int = when (this) {
    AiTab.RHYMES -> R.string.ai_tab_rhymes
    AiTab.REPHRASE -> R.string.ai_tab_rephrase
    AiTab.CRITIQUE -> R.string.ai_tab_critique
}
