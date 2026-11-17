package com.griot.feature.editor.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.R

private val TileHeight = 72.dp
private val TileRadius = 20.dp

/** « Fichier » and « Lien », side by side at the bottom of the audios screen. */
@Composable
internal fun AddButtons(onFile: () -> Unit, onLink: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = GriotSpacing.xl, vertical = GriotSpacing.l),
        horizontalArrangement = Arrangement.spacedBy(GriotSpacing.s + 2.dp),
    ) {
        Tile(icon = DesignR.drawable.ic_folder, label = stringResource(R.string.audios_add_file), onClick = onFile)
        Tile(icon = DesignR.drawable.ic_link, label = stringResource(R.string.audios_add_link), onClick = onLink)
    }
}

@Composable
private fun RowScope.Tile(icon: Int, label: String, onClick: () -> Unit) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(TileRadius)
    Column(
        modifier = Modifier
            .weight(1f)
            .height(TileHeight)
            .clip(shape)
            .background(palette.sheet, shape)
            .border(1.dp, palette.line, shape)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = palette.ink)
        Text(text = label, style = GriotTheme.type.smallStrong, color = palette.ink)
    }
}
