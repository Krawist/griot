package com.griot.core.data.model

/**
 * What an import created.
 *
 * @property songs Number of songs created.
 * @property sections Number of sections in those songs, repeats included.
 * @property failed Names of the files that were empty or could not be read, in order: they created nothing.
 */
data class ImportResult(val songs: Int, val sections: Int, val failed: List<String>)
