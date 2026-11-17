package com.griot.feature.library.trash

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.TrashedSong
import com.griot.core.design.R as DesignR
import com.griot.core.design.format.RelativeTime
import com.griot.core.design.format.text
import com.griot.core.design.glass.GlassSurface
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.library.R
import java.time.Instant

private val BackOffset = (-10).dp

/** The trash: the songs deleted in the last 30 days, which can be restored. */
@Composable
internal fun TrashScreen(onBack: () -> Unit, viewModel: TrashViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrashContent(uiState = uiState, onBack = onBack, onRestore = viewModel::onRestore)
}

@Composable
private fun TrashContent(uiState: TrashUiState, onBack: () -> Unit, onRestore: (String) -> Unit) {
    val palette = GriotTheme.palette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(start = GriotSpacing.xl, top = GriotSpacing.s, end = GriotSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(GriotSpacing.m),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.offset(x = BackOffset).size(GriotSpacing.touchTarget)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_back),
                contentDescription = stringResource(R.string.trash_back),
                tint = palette.ink,
            )
        }
        Text(
            text = stringResource(R.string.trash_title),
            style = MaterialTheme.typography.headlineLarge,
            color = palette.ink,
        )
        Text(
            text = stringResource(R.string.trash_body),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.ink2,
        )
        when {
            uiState.loading -> Unit
            uiState.items.isEmpty() -> Text(
                text = stringResource(R.string.trash_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = GriotSpacing.xxl * 2),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(top = GriotSpacing.s, bottom = GriotSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(GriotSpacing.m - 2.dp),
            ) {
                items(items = uiState.items, key = { it.song.id }) { item ->
                    TrashRow(item = item, onRestore = { onRestore(item.song.id) }, modifier = Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun TrashRow(item: TrashItem, onRestore: () -> Unit, modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    GlassSurface(shape = RoundedCornerShape(GriotRadius.card), modifier = modifier.fillMaxWidth(), blur = false) {
        Row(
            modifier = Modifier.padding(
                start = GriotSpacing.l,
                top = GriotSpacing.m,
                end = GriotSpacing.xs,
                bottom = GriotSpacing.m,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GriotSpacing.xs)) {
                Text(
                    text = item.song.title.ifBlank { stringResource(R.string.library_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.trash_meta,
                        item.deleted.text(),
                        pluralStringResource(R.plurals.trash_days_left, item.daysLeft.toInt(), item.daysLeft),
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = palette.muted,
                )
            }
            TextButton(
                onClick = onRestore,
                colors = ButtonDefaults.textButtonColors(contentColor = palette.accentText),
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_restore),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.trash_restore),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(start = GriotSpacing.xs),
                )
            }
        }
    }
}

@Preview(name = "Light", heightDp = 844, widthDp = 390)
@Preview(name = "Dark", heightDp = 844, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrashScreenPreview() {
    GriotTheme {
        Box(Modifier.background(GriotTheme.palette.background)) {
            TrashContent(
                uiState = TrashUiState(
                    items = listOf(
                        TrashItem(
                            song = TrashedSong(id = "1", title = "Les néons", deletedAt = Instant.EPOCH),
                            deleted = RelativeTime.Yesterday,
                            daysLeft = 29,
                        ),
                    ),
                    loading = false,
                ),
                onBack = {},
                onRestore = {},
            )
        }
    }
}
