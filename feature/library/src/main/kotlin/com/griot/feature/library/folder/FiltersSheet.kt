package com.griot.feature.library.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.SongLanguage
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.GriotSwitch
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.FilterChip
import com.griot.feature.library.R
import com.griot.feature.library.displayName

private val CardShape = RoundedCornerShape(16.dp)

/**
 * « Filtres et tri »: the order, the genres (several), the language, the folder where songs of several folders mix,
 * the states (several) and the archives. Nothing applies before « Voir N textes », which counts the songs kept as the
 * author chooses; « Réinitialiser » clears every filter.
 */
@Composable
internal fun FiltersSheet(
    state: FilterSheetState,
    onChange: (FolderFilters) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    val draft = state.draft
    GriotSheet(onDismiss = onDismiss) {
        Header(onReset = onReset)
        Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.l)) {
            Group(label = stringResource(R.string.folder_sort)) {
                Segments(
                    options = SongSort.entries.map { it to stringResource(it.labelRes()) },
                    selected = draft.sort,
                    onSelect = { onChange(draft.copy(sort = it)) },
                )
            }
            GenreChoice(state = state, onChange = onChange)
            Group(label = stringResource(R.string.folder_language)) {
                Segments(
                    options = listOf(
                        null to stringResource(R.string.folder_all_languages),
                        SongLanguage.FRENCH to stringResource(R.string.library_filter_french),
                        SongLanguage.ENGLISH to stringResource(R.string.library_filter_english),
                    ),
                    selected = draft.language,
                    onSelect = { onChange(draft.copy(language = it)) },
                )
            }
            if (state.folders.isNotEmpty()) FolderChoice(state = state, onChange = onChange)
            StateChoice(draft = draft, onChange = onChange)
            if (state.showArchives) {
                ArchivesSwitch(
                    count = state.archivesCount,
                    included = draft.includeArchives,
                    onChange = { onChange(draft.copy(includeArchives = it)) },
                )
            }
        }
        GriotButton(
            text = if (state.count == 0) {
                stringResource(R.string.folder_show_none)
            } else {
                pluralStringResource(R.plurals.folder_show, state.count, state.count)
            },
            enabled = state.count > 0,
            onClick = onApply,
            modifier = Modifier.padding(top = GriotSpacing.m),
        )
    }
}

@Composable
private fun Header(onReset: () -> Unit) {
    val palette = GriotTheme.palette
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.folder_filters),
            style = GriotTheme.type.sheetTitle,
            color = palette.ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.folder_filters_reset),
            style = GriotTheme.type.label,
            color = palette.accentText,
            modifier = Modifier
                .heightIn(min = GriotSpacing.touchTarget)
                .clip(RoundedCornerShape(GriotSpacing.s))
                .clickable(role = Role.Button, onClick = onReset)
                .padding(horizontal = GriotSpacing.s, vertical = GriotSpacing.m),
        )
    }
}

// Every genre; several can be kept.
@Composable
private fun GenreChoice(state: FilterSheetState, onChange: (FolderFilters) -> Unit) {
    val draft = state.draft
    Group(label = stringResource(R.string.folder_filter_genres)) {
        Chips {
            state.genres.forEach { genre ->
                FilterChip(
                    label = genre.displayName(),
                    selected = genre.id in draft.genres,
                    accent = true,
                    onClick = { onChange(draft.copy(genres = draft.genres.toggle(genre.id))) },
                )
            }
        }
    }
}

// « Brouillon », « En cours » and « Sans titre »; several can be kept.
@Composable
private fun StateChoice(draft: FolderFilters, onChange: (FolderFilters) -> Unit) {
    Group(label = stringResource(R.string.folder_filter_state)) {
        Chips {
            SongState.entries.forEach { songState ->
                FilterChip(
                    label = stringResource(songState.labelRes()),
                    selected = songState in draft.states,
                    accent = true,
                    onClick = { onChange(draft.copy(states = draft.states.toggle(songState))) },
                )
            }
        }
    }
}

// « Tous », « Sans dossier » and each folder; one at most.
@Composable
private fun FolderChoice(state: FilterSheetState, onChange: (FolderFilters) -> Unit) {
    val draft = state.draft
    Group(label = stringResource(R.string.folder_filter_folder)) {
        Chips {
            listOf(
                null to stringResource(R.string.folder_all_genres),
                FolderFilters.NO_FOLDER to stringResource(R.string.library_no_folder),
            ).forEach { (folder, label) ->
                FilterChip(
                    label = label,
                    selected = draft.folder == folder,
                    accent = true,
                    onClick = { onChange(draft.copy(folder = folder)) },
                )
            }
            state.folders.forEach { folder ->
                FilterChip(
                    label = folder.displayName(),
                    selected = draft.folder == folder.id,
                    accent = true,
                    onClick = { onChange(draft.copy(folder = folder.id)) },
                )
            }
        }
    }
}

// « Inclure les archives », with how many songs « Archives » holds.
@Composable
private fun ArchivesSwitch(count: Int, included: Boolean, onChange: (Boolean) -> Unit) {
    val palette = GriotTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(palette.glass, CardShape)
            .border(1.dp, palette.line, CardShape)
            .clickable(role = Role.Switch) { onChange(!included) }
            .padding(horizontal = GriotSpacing.m + 2.dp, vertical = GriotSpacing.s + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = stringResource(R.string.folder_include_archives),
                style = GriotTheme.type.bodyStrong,
                color = palette.ink,
            )
            Text(
                text = pluralStringResource(R.plurals.folder_archives_count, count, count),
                style = GriotTheme.type.caption,
                color = palette.muted,
            )
        }
        GriotSwitch(checked = included, onCheckedChange = onChange)
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

private fun SongSort.labelRes(): Int = when (this) {
    SongSort.MODIFIED -> R.string.folder_sort_modified
    SongSort.CREATED -> R.string.folder_sort_created
    SongSort.TITLE -> R.string.folder_sort_title
}

private fun SongState.labelRes(): Int = when (this) {
    SongState.DRAFT -> R.string.folder_state_draft
    SongState.IN_PROGRESS -> R.string.folder_state_in_progress
    SongState.UNTITLED -> R.string.library_untitled
}
