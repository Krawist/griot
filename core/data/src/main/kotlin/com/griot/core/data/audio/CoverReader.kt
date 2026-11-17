package com.griot.core.data.audio

/** Reads the cover of an audio file and the player's colours from it. */
interface CoverReader {
    /** The embedded artwork of [uri] with its colours; Griot's own colours without artwork. */
    suspend fun read(uri: String): CoverArt
}
