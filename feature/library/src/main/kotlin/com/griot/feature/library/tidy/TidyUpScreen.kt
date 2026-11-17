package com.griot.feature.library.tidy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.TidyUpKind
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.BackButton
import com.griot.core.design.component.GriotButton
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.R
import com.griot.feature.library.SectionLabel

private val RowHeight = 64.dp
private val DecisionSize = 40.dp
private val DecisionRadius = 12.dp
private val ListBottom = 140.dp
private const val REFUSED_ALPHA = 0.6f

// The groups in order, and whether their proposals come from the AI.
private val Groups = listOf(
    TidyUpKind.CLASSIFY to true,
    TidyUpKind.TITLE to true,
    TidyUpKind.DUPLICATE to false,
    TidyUpKind.ARCHIVE to false,
)

/** « Propositions de rangement »: each proposal accepted (✓) or refused (✕) on its own, or all the rest at once. */
@Composable
internal fun TidyUpScreen(onBack: () -> Unit, viewModel: TidyUpViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pending = uiState.proposals.count { it.decision == null }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = GriotSpacing.xl, top = GriotSpacing.s, end = GriotSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(GriotSpacing.l),
        ) {
            Header(pending = pending, onBack = onBack)
            LazyColumn(
                contentPadding = PaddingValues(bottom = ListBottom),
                verticalArrangement = Arrangement.spacedBy(GriotSpacing.l),
            ) {
                if (!uiState.loading && uiState.proposals.isEmpty() && !uiState.thinking) {
                    item(key = "empty") { Note(text = stringResource(R.string.tidy_empty)) }
                }
                Groups.forEach { (kind, ai) ->
                    val proposals = uiState.proposals.filter { it.kind == kind }
                    if (proposals.isNotEmpty()) {
                        item(key = kind.name) {
                            Group(
                                kind = kind,
                                ai = ai,
                                proposals = proposals,
                                onAccept = viewModel::onAccept,
                                onRefuse = viewModel::onRefuse,
                            )
                        }
                    }
                }
                if (uiState.thinking) item(key = "thinking") { Thinking() }
            }
        }
        BottomActions(pending = pending, onAcceptAll = viewModel::onAcceptAll, onBack = onBack)
    }
}

// « Accepter les N restantes », or « Terminé » once everything is decided, and the reminder that nothing is deleted.
@Composable
private fun BoxScope.BottomActions(pending: Int, onAcceptAll: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(horizontal = GriotSpacing.xl, vertical = GriotSpacing.l),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        GriotButton(
            text = if (pending > 0) {
                pluralStringResource(R.plurals.tidy_accept_all, pending, pending)
            } else {
                stringResource(R.string.tidy_done)
            },
            onClick = if (pending > 0) onAcceptAll else onBack,
        )
        Text(
            text = stringResource(R.string.tidy_nothing_deleted),
            style = GriotTheme.type.caption,
            color = GriotTheme.palette.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Header(pending: Int, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs)) {
        BackButton(description = stringResource(R.string.folder_back), onClick = onBack)
        Column {
            Text(
                text = stringResource(R.string.tidy_title),
                style = GriotTheme.type.sheetTitle,
                color = GriotTheme.palette.ink,
            )
            Text(
                text = pluralStringResource(R.plurals.tidy_pending, pending, pending),
                style = GriotTheme.type.small,
                color = GriotTheme.palette.muted,
            )
        }
    }
}

// The label of a kind and its badge, then its proposals in one card.
@Composable
private fun Group(
    kind: TidyUpKind,
    ai: Boolean,
    proposals: List<TidyUpProposal>,
    onAccept: (String) -> Unit,
    onRefuse: (String) -> Unit,
) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.card)
    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(text = stringResource(kind.titleRes()), modifier = Modifier.weight(1f))
            Badge(
                text = stringResource(if (ai) R.string.tidy_badge_ai else R.string.tidy_badge_griot),
                fill = if (ai) palette.aiSoft else palette.syncSoft,
                color = if (ai) palette.aiText else palette.syncText,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(palette.glass, shape)
                .border(1.dp, palette.line, shape),
        ) {
            proposals.forEachIndexed { index, proposal ->
                if (index > 0) HorizontalDivider(color = palette.line)
                ProposalRow(proposal = proposal, onAccept = onAccept, onRefuse = onRefuse)
            }
        }
    }
}

@Composable
private fun ProposalRow(proposal: TidyUpProposal, onAccept: (String) -> Unit, onRefuse: (String) -> Unit) {
    val palette = GriotTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .alpha(if (proposal.decision == TidyUpDecision.REFUSED) REFUSED_ALPHA else 1f)
            .padding(
                start = GriotSpacing.m + 2.dp,
                top = GriotSpacing.s + 2.dp,
                end = GriotSpacing.m,
                bottom =
                GriotSpacing.s + 2.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = proposal.heading(),
                style = GriotTheme.type.bodyStrong,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = proposal.detail(), style = GriotTheme.type.small, color = palette.ink2)
        }
        when (proposal.decision) {
            null -> {
                DecisionButton(
                    icon = DesignR.drawable.ic_close,
                    description = stringResource(R.string.tidy_refuse),
                    filled = false,
                    onClick = { onRefuse(proposal.id) },
                )
                DecisionButton(
                    icon = DesignR.drawable.ic_check,
                    description = stringResource(R.string.tidy_accept),
                    filled = true,
                    onClick = { onAccept(proposal.id) },
                )
            }
            TidyUpDecision.ACCEPTED ->
                Badge(text = stringResource(R.string.tidy_accepted), fill = palette.syncSoft, color = palette.syncText)
            TidyUpDecision.REFUSED ->
                Badge(text = stringResource(R.string.tidy_refused), fill = palette.track, color = palette.muted)
        }
    }
}

@Composable
private fun DecisionButton(icon: Int, description: String, filled: Boolean, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(DecisionRadius)
    Box(
        modifier = Modifier
            .size(DecisionSize)
            .clip(shape)
            .then(
                if (filled) Modifier.background(palette.inverse, shape) else Modifier.border(1.dp, palette.line, shape),
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (filled) palette.onInverse else palette.ink2,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun Badge(text: String, fill: androidx.compose.ui.graphics.Color, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = GriotTheme.type.tag,
        color = color,
        modifier = Modifier
            .background(fill, RoundedCornerShape(GriotRadius.badge))
            .padding(horizontal = GriotSpacing.s, vertical = 2.dp),
    )
}

@Composable
private fun Thinking() {
    Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
        Note(text = stringResource(R.string.tidy_thinking))
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = GriotTheme.palette.ai,
            trackColor = GriotTheme.palette.aiSoft,
        )
    }
}

@Composable
private fun Note(text: String) {
    Text(text = text, style = GriotTheme.type.body, color = GriotTheme.palette.ink2)
}
