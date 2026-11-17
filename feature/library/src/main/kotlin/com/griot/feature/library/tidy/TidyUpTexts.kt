package com.griot.feature.library.tidy

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.TidyUpKind
import com.griot.feature.library.R
import com.griot.feature.library.displayName

// The first line of a proposal: the song, or « 4 brouillons ».
@Composable
internal fun TidyUpProposal.heading(): String = when (kind) {
    TidyUpKind.ARCHIVE -> pluralStringResource(R.plurals.tidy_drafts, songIds.size, songIds.size)
    else -> songTitle.ifBlank { stringResource(R.string.library_untitled) }
}

// What a proposal changes: « → dossier EP 2027 · genre Afro », « « Ce soir encore, la radio » → « Ce soir encore » »…
@Composable
internal fun TidyUpProposal.detail(): String = when (kind) {
    TidyUpKind.CLASSIFY -> listOfNotNull(
        folder?.let { stringResource(R.string.tidy_to_folder, it.displayName()) },
        genre?.let { stringResource(R.string.tidy_to_genre, it.displayName()) },
    ).joinToString(separator = " · ", prefix = "→ ")
    TidyUpKind.TITLE -> stringResource(R.string.tidy_to_title, firstVerse.orEmpty(), title.orEmpty())
    TidyUpKind.DUPLICATE -> pluralStringResource(R.plurals.tidy_duplicates, songIds.size, songIds.size)
    TidyUpKind.ARCHIVE -> stringResource(R.string.tidy_to_archives)
}

internal fun TidyUpKind.titleRes(): Int = when (this) {
    TidyUpKind.CLASSIFY -> R.string.tidy_classify
    TidyUpKind.TITLE -> R.string.tidy_titles
    TidyUpKind.DUPLICATE -> R.string.tidy_duplicates_title
    TidyUpKind.ARCHIVE -> R.string.tidy_archive
}
