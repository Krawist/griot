package com.griot.feature.library.tidy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.ai.AiAssistant
import com.griot.core.data.model.FolderKind
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.TidyUpKind
import com.griot.core.data.organisation.FolderRepository
import com.griot.core.data.organisation.GenreRepository
import com.griot.core.data.song.SongRepository
import com.griot.core.data.tidy.TidyUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The tidy-up proposals: duplicates and old drafts found by code at once, then titles and classifications from the
 * AI as they come. Nothing changes before the author accepts; a refused proposal never comes back.
 */
@HiltViewModel
internal class TidyUpViewModel @Inject constructor(
    private val tidy: TidyUpRepository,
    private val songs: SongRepository,
    private val folders: FolderRepository,
    private val genres: GenreRepository,
    private val assistant: AiAssistant,
) : ViewModel() {
    private val state = MutableStateFlow(TidyUpUiState())

    val uiState: StateFlow<TidyUpUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            val candidates = tidy.observeCandidates().first()
            val duplicates = candidates.duplicates.map { group ->
                TidyUpProposal(
                    id = "${TidyUpKind.DUPLICATE}:${TidyUpRepository.duplicateKey(group.map { it.id })}",
                    kind = TidyUpKind.DUPLICATE,
                    songIds = group.map { it.id },
                    songTitle = group.first().title,
                )
            }
            val archive = candidates.staleDrafts.takeIf { it.isNotEmpty() }?.let { drafts ->
                TidyUpProposal(
                    id = "${TidyUpKind.ARCHIVE}:${drafts.joinToString(",") { it.id }}",
                    kind = TidyUpKind.ARCHIVE,
                    songIds = drafts.map { it.id },
                    songTitle = "",
                )
            }
            val ai = assistant.availability().location != null
            state.value = TidyUpUiState(proposals = duplicates + listOfNotNull(archive), thinking = ai, loading = false)
            if (ai) {
                askAi(
                    untitled = candidates.untitled.take(AI_SONGS),
                    unclassified = candidates.unclassified.take(AI_SONGS),
                )
            }
        }
    }

    // Titles, then classifications, each added as its answer comes.
    private suspend fun askAi(untitled: List<LibrarySong>, unclassified: List<LibrarySong>) {
        untitled.forEach { song ->
            assistant.suggestTitle(song.request())?.let { title ->
                add(
                    TidyUpProposal(
                        id = "${TidyUpKind.TITLE}:${song.id}",
                        kind = TidyUpKind.TITLE,
                        songIds = listOf(song.id),
                        songTitle = song.title,
                        firstVerse = song.firstVerse,
                        title = title,
                    ),
                )
            }
        }
        val customFolders = folders.observeFolders().first().filter { it.kind == FolderKind.CUSTOM }
        val allGenres = genres.observeGenres().first()
        unclassified.forEach { song ->
            val request = song.request(folders = customFolders, genres = allGenres)
            val classification = assistant.classify(request) ?: return@forEach
            add(
                TidyUpProposal(
                    id = "${TidyUpKind.CLASSIFY}:${song.id}",
                    kind = TidyUpKind.CLASSIFY,
                    songIds = listOf(song.id),
                    songTitle = song.title,
                    folder = customFolders.firstOrNull { it.name == classification.folder },
                    genre = allGenres.firstOrNull { it.modelName() == classification.genre },
                ),
            )
        }
        state.update { it.copy(thinking = false) }
    }

    private fun add(proposal: TidyUpProposal) {
        state.update { current -> current.copy(proposals = current.proposals + proposal) }
    }

    /** Applies the proposal [id]. */
    fun onAccept(id: String) {
        val proposal = pending(id) ?: return
        decide(id, TidyUpDecision.ACCEPTED)
        viewModelScope.launch { apply(proposal) }
    }

    /** Refuses the proposal [id]: it is never proposed again. */
    fun onRefuse(id: String) {
        val proposal = pending(id) ?: return
        decide(id, TidyUpDecision.REFUSED)
        val keys = when (proposal.kind) {
            TidyUpKind.DUPLICATE -> listOf(TidyUpRepository.duplicateKey(proposal.songIds))
            else -> proposal.songIds
        }
        viewModelScope.launch { tidy.refuse(kind = proposal.kind, keys = keys) }
    }

    /** « Accepter les N restantes ». */
    fun onAcceptAll() {
        state.value.proposals.filter { it.decision == null }.forEach { onAccept(it.id) }
    }

    private fun pending(id: String) = state.value.proposals.firstOrNull { it.id == id && it.decision == null }

    private fun decide(id: String, decision: TidyUpDecision) {
        state.update { current ->
            current.copy(proposals = current.proposals.map { if (it.id == id) it.copy(decision = decision) else it })
        }
        viewModelScope.launch { reviewIfDone() }
    }

    // Every proposal decided (or none to make): the next tidy-up comes after an import, or a week later. Leaving
    // with proposals waiting keeps the card.
    private suspend fun reviewIfDone() {
        val current = state.value
        if (!current.loading && !current.thinking && current.proposals.none { it.decision == null }) tidy.markReviewed()
    }

    private suspend fun apply(proposal: TidyUpProposal) {
        when (proposal.kind) {
            TidyUpKind.CLASSIFY -> {
                proposal.folder?.let {
                    folders.fileSongs(songIds = proposal.songIds, add = setOf(it.id), remove = emptySet())
                }
                proposal.genre?.let {
                    genres.tagSongs(songIds = proposal.songIds, add = setOf(it.id), remove = emptySet())
                }
            }
            TidyUpKind.TITLE -> proposal.title?.let { songs.renameSong(id = proposal.songIds.first(), title = it) }
            TidyUpKind.DUPLICATE ->
                tidy.mergeDuplicates(keepId = proposal.songIds.first(), duplicates = proposal.songIds.drop(1))
            TidyUpKind.ARCHIVE -> folders.fileSongs(
                songIds = proposal.songIds,
                add = setOf(FolderRepository.ARCHIVES_ID),
                remove = emptySet(),
            )
        }
    }
}
