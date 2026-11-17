package com.griot.core.data.audio

/** [CoverReader] for tests: every file has no artwork, so Griot's own colours. */
class FakeCoverReader : CoverReader {
    override suspend fun read(uri: String): CoverArt = CoverArt(image = null, colors = PlayerColors.Griot)
}
