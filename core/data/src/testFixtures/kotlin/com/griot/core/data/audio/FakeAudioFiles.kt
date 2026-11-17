package com.griot.core.data.audio

/** [AudioFiles] for tests: the files whose address is in [missing] cannot be read; the others can. */
class FakeAudioFiles(private val missing: Set<String> = emptySet()) : AudioFiles {
    override suspend fun take(uri: String): AudioFileInfo? =
        AudioFileInfo(title = uri, durationMillis = null).takeUnless { uri in missing }

    override suspend fun isAvailable(uri: String): Boolean = uri !in missing
}
