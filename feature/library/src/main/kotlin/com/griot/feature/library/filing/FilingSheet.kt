package com.griot.feature.library.filing

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.FolderColor
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.TextInputForm
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.FilterChip
import com.griot.feature.library.R
import com.griot.feature.library.SectionLabel
import com.griot.feature.library.displayName
import com.griot.feature.library.tint

private val RowHeight = 48.dp
private val IconBox = 30.dp
private val IconRadius = 9.dp
private val CheckSize = 22.dp
private val AddGenreSize = 36.dp

/** What the sheet shows: the folders and genres, or the name of a new folder or genre. */
private enum class FilingStep {
    CHOICES,
    NEW_FOLDER,
    NEW_GENRE,
}

/**
 * « Dossier et genre »: files the songs [songIds] in folders (several at once) and gives them a genre (one at most),
 * applied on « Valider ». From the editor's « ⋯ », a long press on a song, and « Ranger » in the multiple selection.
 */
@Composable
fun FilingSheet(songIds: List<String>, onDismiss: () -> Unit) {
    FilingSheet(
        songIds = songIds,
        onDismiss = onDismiss,
        viewModel = hiltViewModel<FilingViewModel, FilingViewModel.Factory>(
            key = songIds.joinToString(),
            creationCallback = { factory -> factory.create(songIds) },
        ),
    )
}

@Composable
private fun FilingSheet(songIds: List<String>, onDismiss: () -> Unit, viewModel: FilingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var step by rememberSaveable(songIds) { mutableStateOf(FilingStep.CHOICES) }
    GriotSheet(onDismiss = onDismiss) {
        AnimatedContent(targetState = step, label = "filing") { current ->
            when (current) {
                FilingStep.CHOICES -> Choices(
                    uiState = uiState,
                    viewModel = viewModel,
                    onNewFolder = { step = FilingStep.NEW_FOLDER },
                    onNewGenre = { step = FilingStep.NEW_GENRE },
                    onValidate = { viewModel.onValidate(onDone = onDismiss) },
                )
                FilingStep.NEW_FOLDER -> TextInputForm(
                    value = "",
                    label = stringResource(R.string.library_new_folder_label),
                    placeholder = stringResource(R.string.library_new_folder),
                    confirmLabel = stringResource(R.string.library_create),
                    onConfirm = { name ->
                        viewModel.onNewFolder(name)
                        step = FilingStep.CHOICES
                    },
                )
                FilingStep.NEW_GENRE -> TextInputForm(
                    value = "",
                    label = stringResource(R.string.filing_new_genre_label),
                    placeholder = stringResource(R.string.filing_new_genre),
                    confirmLabel = stringResource(R.string.library_create),
                    onConfirm = { name ->
                        viewModel.onNewGenre(name)
                        step = FilingStep.CHOICES
                    },
                )
            }
        }
    }
}

@Composable
private fun Choices(
    uiState: FilingUiState,
    viewModel: FilingViewModel,
    onNewFolder: () -> Unit,
    onNewGenre: () -> Unit,
    onValidate: () -> Unit,
) {
    val palette = GriotTheme.palette
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.l),
    ) {
        Column {
            Text(text = stringResource(R.string.filing_title), style = GriotTheme.type.sheetTitle, color = palette.ink)
            Text(
                text = uiState.title?.ifBlank { stringResource(R.string.library_untitled) }
                    ?: pluralStringResource(R.plurals.library_texts, uiState.songCount, uiState.songCount),
                style = GriotTheme.type.small,
                color = palette.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
            SectionLabel(text = stringResource(R.string.filing_folder))
            Folders(
                folders = uiState.folders,
                onNoFolder = viewModel::onNoFolder,
                onFolder = viewModel::onFolderTapped,
                onNewFolder = onNewFolder,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.s)) {
            SectionLabel(text = stringResource(R.string.filing_genre))
            Genres(uiState = uiState, onGenre = viewModel::onGenreTapped, onNewGenre = onNewGenre)
        }
        GriotButton(text = stringResource(R.string.filing_validate), onClick = onValidate)
    }
}

// « Aucun dossier », the folders with their check, then « Nouveau dossier », in one card.
@Composable
private fun Folders(
    folders: List<FilingFolder>,
    onNoFolder: () -> Unit,
    onFolder: (String) -> Unit,
    onNewFolder: () -> Unit,
) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.field + 2.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.glass, shape)
            .border(1.dp, palette.line, shape),
    ) {
        val none = folders.none { it.check != FolderCheck.UNCHECKED }
        FolderRow(
            name = stringResource(R.string.filing_no_folder),
            color = FolderColor.NEUTRAL,
            check = if (none) FolderCheck.CHECKED else FolderCheck.UNCHECKED,
            onClick = onNoFolder,
        )
        folders.forEach { filing ->
            HorizontalDivider(color = palette.line)
            FolderRow(
                name = filing.folder.displayName(),
                color = filing.folder.color,
                check = filing.check,
                onClick = { onFolder(filing.folder.id) },
            )
        }
        HorizontalDivider(color = palette.line)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = RowHeight + 4.dp)
                .clickable(role = Role.Button, onClick = onNewFolder)
                .padding(horizontal = GriotSpacing.m + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_add),
                contentDescription = null,
                tint = palette.accentText,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.library_new_folder),
                style = GriotTheme.type.bodyStrong,
                color = palette.accentText,
            )
        }
    }
}

@Composable
private fun FolderRow(name: String, color: FolderColor, check: FolderCheck, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val (fill, content) = color.tint()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = GriotSpacing.m + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
    ) {
        Box(
            modifier = Modifier.size(IconBox).background(fill, RoundedCornerShape(IconRadius)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_folder),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(15.dp),
            )
        }
        Text(
            text = name,
            style = GriotTheme.type.body,
            color = palette.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Check(check = check)
    }
}

// A filled magenta circle with a check (or a dash for some of the songs only), an empty ring otherwise.
@Composable
private fun Check(check: FolderCheck) {
    val palette = GriotTheme.palette
    val description = stringResource(
        when (check) {
            FolderCheck.CHECKED -> R.string.filing_checked
            FolderCheck.UNCHECKED -> R.string.filing_unchecked
            FolderCheck.MIXED -> R.string.filing_mixed
        },
    )
    val on = check != FolderCheck.UNCHECKED
    Box(
        modifier = Modifier
            .size(CheckSize)
            .background(if (on) palette.accent else Color.Transparent, CircleShape)
            .then(if (on) Modifier else Modifier.border(2.dp, palette.radioOff, CircleShape))
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (on) {
            Icon(
                painter = painterResource(
                    if (check ==
                        FolderCheck.MIXED
                    ) {
                        DesignR.drawable.ic_minus
                    } else {
                        DesignR.drawable.ic_check
                    },
                ),
                contentDescription = null,
                tint = palette.onAccent,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

// The genres as chips, the chosen one filled, then « + » for a genre of the author.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Genres(uiState: FilingUiState, onGenre: (String) -> Unit, onNewGenre: () -> Unit) {
    val palette = GriotTheme.palette
    val description = stringResource(R.string.filing_new_genre)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        uiState.genres.forEach { genre ->
            FilterChip(label = genre.displayName(), selected = uiState.genre == genre.id, onClick = {
                onGenre(genre.id)
            })
        }
        Box(
            modifier = Modifier
                .size(AddGenreSize)
                .clip(CircleShape)
                .border(1.5.dp, palette.line, CircleShape)
                .clickable(role = Role.Button, onClick = onNewGenre)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_add),
                contentDescription = null,
                tint = palette.ink2,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
