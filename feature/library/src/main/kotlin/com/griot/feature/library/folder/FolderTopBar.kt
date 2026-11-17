package com.griot.feature.library.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griot.core.data.model.FolderKind
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotMenu
import com.griot.core.design.component.GriotMenuItem
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.R
import com.griot.feature.library.displayName

/**
 * Back, the folder's name (or « Tous les textes ») and its count, « Sélectionner », and the folder's « ⋯ » menu for a
 * folder of the author. In the multiple selection: the count selected and « Annuler ».
 */
@Composable
internal fun FolderTopBar(
    uiState: FolderUiState,
    onBack: () -> Unit,
    onSelecting: (Boolean) -> Unit,
    onSheet: (FolderSheet) -> Unit,
) {
    val palette = GriotTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(GriotSpacing.touchTarget)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_back),
                contentDescription = stringResource(R.string.folder_back),
                tint = palette.ink,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (uiState.selecting) {
                    pluralStringResource(R.plurals.folder_selected, uiState.selected.size, uiState.selected.size)
                } else {
                    uiState.folder?.displayName() ?: stringResource(R.string.folder_all_texts)
                },
                style = GriotTheme.type.pageTitle,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!uiState.selecting) {
                val count = pluralStringResource(R.plurals.library_texts, uiState.songCount, uiState.songCount)
                Text(
                    text = if (uiState.folder != null) stringResource(R.string.folder_subtitle, count) else count,
                    style = GriotTheme.type.small,
                    color = palette.muted,
                )
            }
        }
        TextAction(
            text = stringResource(if (uiState.selecting) R.string.folder_cancel else R.string.folder_select),
            onClick = { onSelecting(!uiState.selecting) },
        )
        if (!uiState.selecting && uiState.folder?.kind == FolderKind.CUSTOM) FolderMenu(onSheet = onSheet)
    }
}

// « ⋯ » and its menu: « Renommer », « Changer la couleur », « Supprimer le dossier ».
@Composable
private fun FolderMenu(onSheet: (FolderSheet) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val pick = { sheet: FolderSheet ->
        open = false
        onSheet(sheet)
    }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.size(GriotSpacing.touchTarget)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_more),
                contentDescription = stringResource(R.string.folder_menu),
                tint = GriotTheme.palette.ink,
            )
        }
        GriotMenu(expanded = open, onDismiss = { open = false }) {
            GriotMenuItem(label = stringResource(R.string.folder_rename), onClick = { pick(FolderSheet.RENAME) })
            GriotMenuItem(label = stringResource(R.string.folder_color), onClick = { pick(FolderSheet.COLOR) })
            GriotMenuItem(
                label = stringResource(R.string.folder_delete),
                onClick = { pick(FolderSheet.DELETE) },
                danger = true,
            )
        }
    }
}

@Composable
private fun TextAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = GriotTheme.type.textButton,
        color = GriotTheme.palette.accentText,
        modifier = Modifier
            .clip(RoundedCornerShape(GriotRadius.control))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = GriotSpacing.s, vertical = GriotSpacing.m),
    )
}
