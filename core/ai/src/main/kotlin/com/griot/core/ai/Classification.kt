package com.griot.core.ai

/**
 * Where a model would file a song: always among the names it was given.
 *
 * @property folder The name of the folder; `null` when none fits.
 * @property genre The name of the genre; `null` when none fits.
 */
data class Classification(val folder: String?, val genre: String?)
