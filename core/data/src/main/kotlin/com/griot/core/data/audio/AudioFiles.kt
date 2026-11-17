package com.griot.core.data.audio

/** The audio files of the phone, read where they are: Griot never copies them. */
interface AudioFiles {
    /**
     * Keeps the right to read the file [uri] picked by the author, and reads its name and length; `null` when it
     * cannot be read.
     */
    suspend fun take(uri: String): AudioFileInfo?

    /** Whether the file [uri] can still be read: moved or deleted, it cannot. */
    suspend fun isAvailable(uri: String): Boolean
}
