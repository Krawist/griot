package com.griot.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

// The dictionaries inside Griot: their name, their authors and their licence.
private val Dictionaries = listOf(
    Triple(R.string.settings_licence_lexique, R.string.settings_licence_lexique_authors, R.string.licence_cc_by_sa),
    Triple(R.string.settings_licence_cmu, R.string.settings_licence_cmu_authors, R.string.licence_bsd),
    Triple(
        R.string.settings_licence_frequency,
        R.string.settings_licence_frequency_authors,
        R.string.licence_cc_by_sa,
    ),
)

// The fonts inside Griot: their name, their authors and their licence.
private val Fonts = listOf(
    Triple(R.string.settings_licence_outfit, R.string.settings_licence_outfit_authors, R.string.licence_ofl),
    Triple(R.string.settings_licence_inter, R.string.settings_licence_inter_authors, R.string.licence_ofl),
)

/**
 * The free works inside Griot, in glass groups as in the settings: « Dictionnaires » and « Polices », each work with
 * its name, its authors and its licence in a pill.
 */
@Composable
internal fun LicencesSheet(onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    GriotSheet(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.xs)) {
            Text(
                text = stringResource(R.string.settings_licences),
                style = GriotTheme.type.sheetTitle,
                color = palette.ink,
            )
            Text(
                text = stringResource(R.string.settings_licences_thanks),
                style = GriotTheme.type.description,
                color = palette.muted,
            )
        }
        Credits(title = R.string.settings_licences_dictionaries, credits = Dictionaries, tone = SettingsTone.SYNC)
        Credits(title = R.string.settings_licences_fonts, credits = Fonts, tone = SettingsTone.BRAND)
    }
}

// One group of works, a row each: its icon, its name, its authors, its licence.
@Composable
private fun Credits(title: Int, credits: List<Triple<Int, Int, Int>>, tone: SettingsTone) {
    SettingsGroup(title = stringResource(title)) {
        credits.forEachIndexed { index, (name, authors, licence) ->
            SettingsRow(
                letter = stringResource(R.string.settings_licence_font_letter),
                icon = if (tone == SettingsTone.SYNC) DesignR.drawable.ic_rhymes else null,
                tone = tone,
                label = stringResource(name),
                sub = stringResource(authors),
                first = index == 0,
            ) { SettingsPill(label = stringResource(licence), tone = tone) }
        }
    }
}
