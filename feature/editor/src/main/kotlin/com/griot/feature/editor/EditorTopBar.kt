package com.griot.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griot.core.data.model.SongLanguage
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val ChipHeight = 32.dp

/** The editor's top bar: back, the song's title edited in place, the « Aa » menu and the song's language. */
@Composable
internal fun EditorTopBar(
    title: String,
    language: SongLanguage,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onFontSize: () -> Unit,
) {
    val palette = GriotTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = GriotSpacing.s, top = GriotSpacing.xs, end = GriotSpacing.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.xs),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(GriotSpacing.touchTarget)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_back),
                contentDescription = stringResource(R.string.editor_back),
                tint = palette.ink,
            )
        }
        SyncedTextField(
            text = title,
            onTextChange = onTitleChange,
            style = MaterialTheme.typography.titleLarge,
            placeholder = stringResource(R.string.editor_title_placeholder),
            modifier = Modifier.weight(1f),
        )
        FontSizeButton(onClick = onFontSize)
        LanguageChip(language = language)
    }
}

@Composable
private fun FontSizeButton(onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val description = stringResource(R.string.editor_font_size)
    Box(
        modifier = Modifier
            .size(GriotSpacing.touchTarget)
            .clip(RoundedCornerShape(GriotRadius.control))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.editor_font_size_button),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = palette.ink,
        )
    }
}

@Composable
private fun LanguageChip(language: SongLanguage) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.control)
    val code = stringResource(
        when (language) {
            SongLanguage.FRENCH -> R.string.editor_language_french
            SongLanguage.ENGLISH -> R.string.editor_language_english
            SongLanguage.AUTOMATIC -> R.string.editor_language_automatic
        },
    )
    val description = stringResource(R.string.editor_language_label, code)
    Box(
        modifier = Modifier
            .height(ChipHeight)
            .background(palette.glass, shape)
            .border(1.dp, palette.line, shape)
            .padding(horizontal = 10.dp)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            color = palette.ink,
        )
    }
}
