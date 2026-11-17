package com.griot.feature.library.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.FilterChip
import com.griot.feature.library.R
import com.griot.feature.library.displayName

private val ButtonHeight = 34.dp

/**
 * « Tous » and the genres present (several can be kept), then the button of « Filtres et tri », in magenta with their
 * count while filters are on.
 */
@Composable
internal fun FolderFilterBar(uiState: FolderUiState, onGenreSelected: (String?) -> Unit, onOpenFilters: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s),
        ) {
            FilterChip(
                label = stringResource(R.string.folder_all_genres),
                selected = uiState.filters.genres.isEmpty(),
                onClick = { onGenreSelected(null) },
            )
            uiState.genres.forEach { genre ->
                FilterChip(
                    label = genre.displayName(),
                    selected = genre.id in uiState.filters.genres,
                    onClick = { onGenreSelected(genre.id) },
                )
            }
        }
        FilterButton(count = uiState.filterCount, onClick = onOpenFilters)
    }
}

@Composable
private fun FilterButton(count: Int, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val on = count > 0
    val description = pluralStringResource(R.plurals.folder_filters_button, count, count)
    Row(
        modifier = Modifier
            .height(ButtonHeight)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .background(if (on) palette.accentSoft else palette.glass, CircleShape)
            .then(if (on) Modifier else Modifier.border(1.dp, palette.line, CircleShape))
            .padding(horizontal = GriotSpacing.s + 2.dp)
            .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs),
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_filter),
            contentDescription = null,
            tint = if (on) palette.accentText else palette.ink2,
            modifier = Modifier.size(18.dp),
        )
        if (on) Text(text = count.toString(), style = GriotTheme.type.smallStrong, color = palette.accentText)
    }
}
