package com.griot.core.data.song

import com.griot.core.data.model.LibrarySong
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [SongRepository] for tests: [library] is what it lists, [created] the ids of the songs created. */
class FakeSongRepository(library: List<LibrarySong> = emptyList()) : SongRepository {
    val library = MutableStateFlow(library)

    val created = mutableListOf<String>()

    override fun observeLibrary() = library

    override suspend fun createSong(): String {
        val id = "created-song-id-${created.size + 1}"
        created += id
        return id
    }
}
