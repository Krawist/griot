package com.griot.core.data.organisation

import com.griot.core.data.model.Genre
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [GenreRepository] for tests: [genres] is what it lists; [calls] records every change asked, such as
 * `"create my-name"`, in order.
 */
class FakeGenreRepository(genres: List<Genre> = emptyList()) : GenreRepository {
    val genres = MutableStateFlow(genres)

    val calls = mutableListOf<String>()

    override fun observeGenres() = genres

    override suspend fun createGenre(name: String): String {
        calls += "create $name"
        return "created-genre-id"
    }

    override suspend fun renameGenre(id: String, name: String) {
        calls += "rename $id $name"
    }

    override suspend fun deleteGenre(id: String) {
        calls += "delete $id"
    }

    override suspend fun tagSongs(songIds: List<String>, add: Set<String>, remove: Set<String>) {
        calls += "tag ${songIds.joinToString()} +${add.sorted().joinToString()} -${remove.sorted().joinToString()}"
    }
}
