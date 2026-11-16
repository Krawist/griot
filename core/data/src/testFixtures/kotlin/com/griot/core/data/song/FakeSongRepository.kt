package com.griot.core.data.song

import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.NewSection
import com.griot.core.data.model.TrashedSong
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [SongRepository] for tests: [library] and [trash] are what it lists; [calls] records every change asked,
 * such as `"rename my-song-id my-title"`, in order.
 */
class FakeSongRepository(library: List<LibrarySong> = emptyList(), trash: List<TrashedSong> = emptyList()) :
    SongRepository {
    val library = MutableStateFlow(library)

    val trash = MutableStateFlow(trash)

    val calls = mutableListOf<String>()

    override fun observeLibrary() = library

    override fun observeTrash() = trash

    override suspend fun createSong(firstVerse: String): String {
        val id = "created-song-id-${calls.count { it.startsWith("create") } + 1}"
        calls += if (firstVerse.isEmpty()) "create $id" else "create $id $firstVerse"
        return id
    }

    override suspend fun importSong(title: String, sections: List<NewSection>): String {
        calls += "import $title ${sections.size}"
        return "imported-song-id"
    }

    override suspend fun renameSong(id: String, title: String) {
        calls += "rename $id $title"
    }

    override suspend fun duplicateSong(id: String, title: String): String {
        calls += "duplicate $id $title"
        return "copy-of-$id"
    }

    override suspend fun deleteSong(id: String) {
        calls += "delete $id"
    }

    override suspend fun restoreSong(id: String) {
        calls += "restore $id"
    }

    override suspend fun purgeTrash() {
        calls += "purge"
    }
}
