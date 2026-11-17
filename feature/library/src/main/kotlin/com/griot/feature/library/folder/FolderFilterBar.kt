package com.griot.feature.library.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.SongLanguage
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotMenu
import com.griot.core.design.component.GriotMenuItem
import com.griot.core.design.component.GriotMenuLabel
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.FilterChip
import com.griot.feature.library.R
import com.griot.feature.library.displayName

private val ButtonHeight = 34.dp

/** « Tous » and the genres present, then the language filter, which reads « FR » or « EN » when it is on. */
@Composable
internal fun FolderFilterBar(
    uiState: FolderUiState,
    onGenreSelected: (String?) -> Unit,
    onLanguageSelected: (SongLanguage?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        ) {
            FilterChip(
                label = stringResource(R.string.folder_all_genres),
                selected = uiState.genre == null,
                onClick = { onGenreSelected(null) },
            )
            uiState.genres.forEach { genre ->
                FilterChip(
                    label = genre.displayName(),
                    selected = uiState.genre == genre.id,
                    onClick = { onGenreSelected(genre.id) },
                )
            }
        }
        LanguageFilter(language = uiState.language, onLanguageSelected = onLanguageSelected)
    }
}

@Composable
private fun LanguageFilter(language: SongLanguage?, onLanguageSelected: (SongLanguage?) -> Unit) {
    val palette = GriotTheme.palette
    var open by remember { mutableStateOf(false) }
    val description = stringResource(R.string.folder_language_filter)
    val pick = { picked: SongLanguage? ->
        open = false
        onLanguageSelected(picked)
    }
    Box {
        Row(
            modifier = Modifier
                .height(ButtonHeight)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = { open = true })
                .background(if (language != null) palette.accentSoft else palette.glass, CircleShape)
                .then(if (language != null) Modifier else Modifier.border(1.dp, palette.line, CircleShape))
                .padding(horizontal = GriotSpacing.s + 2.dp)
                .semantics { contentDescription = description },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs),
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_filter),
                contentDescription = null,
                tint = if (language != null) palette.accentText else palette.ink2,
                modifier = Modifier.size(18.dp),
            )
            if (language != null) {
                Text(
                    text = stringResource(language.codeRes()),
                    style = GriotTheme.type.smallStrong,
                    color = palette.accentText,
                )
            }
        }
        GriotMenu(expanded = open, onDismiss = { open = false }) {
            GriotMenuLabel(text = stringResource(R.string.folder_language))
            listOf(null, SongLanguage.FRENCH, SongLanguage.ENGLISH).forEach { option ->
                val label = stringResource(option?.nameRes() ?: R.string.folder_all_languages)
                GriotMenuItem(
                    label = if (option == language) stringResource(R.string.folder_checked, label) else label,
                    onClick = { pick(option) },
                )
            }
        }
    }
}

private fun SongLanguage.codeRes(): Int = when (this) {
    SongLanguage.FRENCH -> R.string.library_language_french
    SongLanguage.ENGLISH -> R.string.library_language_english
}

private fun SongLanguage.nameRes(): Int = when (this) {
    SongLanguage.FRENCH -> R.string.library_filter_french
    SongLanguage.ENGLISH -> R.string.library_filter_english
}
