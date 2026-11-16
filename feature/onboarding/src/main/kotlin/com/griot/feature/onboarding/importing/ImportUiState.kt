package com.griot.feature.onboarding.importing

import com.griot.core.data.model.ImportResult

/**
 * What the import screen shows.
 *
 * @property importing Whether texts are being read and turned into songs.
 * @property result What the imports of this screen created so far, added up; `null` before the first one.
 */
internal data class ImportUiState(val importing: Boolean = false, val result: ImportResult? = null)
