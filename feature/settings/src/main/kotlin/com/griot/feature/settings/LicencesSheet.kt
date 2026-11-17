package com.griot.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

// Each work Griot ships: its name, then its authors and licence.
private val Licences = listOf(
    R.string.settings_licence_lexique to R.string.settings_licence_lexique_terms,
    R.string.settings_licence_cmu to R.string.settings_licence_cmu_terms,
    R.string.settings_licence_outfit to R.string.settings_licence_outfit_terms,
    R.string.settings_licence_inter to R.string.settings_licence_inter_terms,
)

/** The dictionaries and fonts inside Griot, with their authors and licences. */
@Composable
internal fun LicencesSheet(onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_licences),
            style = MaterialTheme.typography.titleMedium,
            color = palette.ink,
        )
        Licences.forEach { (name, terms) ->
            Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.xs / 2)) {
                Text(text = stringResource(name), style = MaterialTheme.typography.bodyLarge, color = palette.ink)
                Text(text = stringResource(terms), style = MaterialTheme.typography.bodySmall, color = palette.muted)
            }
        }
    }
}
