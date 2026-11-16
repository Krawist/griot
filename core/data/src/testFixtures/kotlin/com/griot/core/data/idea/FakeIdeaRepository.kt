package com.griot.core.data.idea

import com.griot.core.data.model.Idea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [IdeaRepository] for tests: [ideas] is what it lists; [calls] records every change asked, in order. */
class FakeIdeaRepository(ideas: List<Idea> = emptyList()) : IdeaRepository {
    val ideas = MutableStateFlow(ideas)

    val calls = mutableListOf<String>()

    override fun observeIdeas() = ideas

    override fun observeSongIdeas(songId: String) = ideas.map { list -> list.filter { it.songId == songId } }

    override suspend fun addIdea(text: String): String {
        calls += "add $text"
        return "added-idea-id"
    }

    override suspend fun addImageIdea(uri: String): String? {
        calls += "image $uri"
        return "image-idea-id"
    }

    override suspend fun updateIdea(id: String, text: String) {
        calls += "update $id $text"
    }

    override suspend fun linkIdea(id: String, songId: String?) {
        calls += "link $id $songId"
    }

    override suspend fun startSong(id: String): String {
        calls += "start $id"
        return "started-song-id"
    }

    override suspend fun deleteIdea(id: String) {
        calls += "delete $id"
    }

    override suspend fun restoreIdea(id: String) {
        calls += "restore $id"
    }
}
