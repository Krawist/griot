package com.griot.feature.editor.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.SongSnapshot
import com.griot.core.data.model.SongVerse
import com.griot.core.data.model.VersionSource
import com.griot.core.data.song.SongEditorRepository
import com.griot.core.data.song.VersionRepository
import com.griot.core.design.format.relativeTime
import com.griot.feature.editor.sectionLabels
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The history of the song [songId], opened on the verse [verseId] when set. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = HistoryViewModel.Factory::class)
internal class HistoryViewModel @AssistedInject constructor(
    @Assisted("songId") private val songId: String,
    @Assisted("verseId") private val verseId: String?,
    private val editor: SongEditorRepository,
    private val versions: VersionRepository,
    private val clock: Clock,
) : ViewModel() {
    private val scope = MutableStateFlow(if (verseId != null) HistoryScope.VERSE else HistoryScope.SONG)

    val uiState: StateFlow<HistoryUiState> = scope
        .flatMapLatest { scope ->
            when {
                scope == HistoryScope.VERSE && verseId != null ->
                    combine(editor.observeDocument(songId), versions.observeVerseHistory(verseId)) {
                            document,
                            history,
                        ->
                        verseState(
                            document = document,
                            verseId = verseId,
                            history = history.map {
                                it.id to it.text
                            },
                            records = history.map { Record(it.createdAt, it.source) },
                        )
                    }
                else -> combine(editor.observeDocument(songId), versions.observeSongHistory(songId)) {
                        document,
                        history,
                    ->
                    songState(
                        document = document,
                        history = history.map {
                            it.id to it.snapshot
                        },
                        records = history.map { Record(it.createdAt, it.source) },
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HistoryUiState())

    fun onScopeSelected(selected: HistoryScope) {
        scope.value = selected
    }

    /** Gives the verse, or the song, the text of the version [versionId], as a new version. */
    fun onRestore(versionId: String) {
        viewModelScope.launch {
            if (scope.value == HistoryScope.VERSE) versions.restoreVerse(versionId) else versions.restoreSong(versionId)
        }
    }

    // When and how a version was recorded.
    private data class Record(val at: Instant, val source: VersionSource)

    private fun verseState(
        document: SongDocument?,
        verseId: String,
        history: List<Pair<String, String>>,
        records: List<Record>,
    ): HistoryUiState {
        val section = document?.sections?.firstOrNull { section -> section.verses.any { it.id == verseId } }
        val current = section?.verses?.firstOrNull { it.id == verseId }?.text
        // The text now, then each version: each compared with its neighbour, the newer for the old ones.
        val texts = listOfNotNull(current) + history.map { it.second }
        val entries = texts.mapIndexed { index, text ->
            val neighbour = if (index == 0) texts.getOrNull(1) else texts[index - 1]
            entry(
                index = index - if (current != null) 1 else 0,
                ids = history.map { it.first },
                records = records,
                lines = listOf(HistoryLine(label = null, segments = changedWords(text, neighbour ?: text))),
            )
        }
        return HistoryUiState(
            loading = false,
            scope = HistoryScope.VERSE,
            title = document?.title.orEmpty(),
            verseSection = section?.let { sectionLabels(document.sections)[document.sections.indexOf(it)] },
            verseNumber = section?.verses?.indexOfFirst { it.id == verseId }?.plus(1),
            entries = entries,
        )
    }

    private fun songState(
        document: SongDocument?,
        history: List<Pair<String, SongSnapshot>>,
        records: List<Record>,
    ): HistoryUiState {
        val current = document?.sections
        val contents = listOfNotNull(current) + history.map { it.second.toSections() }
        val entries = contents.mapIndexed { index, sections ->
            val neighbour = if (index == 0) contents.getOrNull(1) else contents[index - 1]
            val others = neighbour.orEmpty().flatMap { it.verses.map { verse -> verse.text } }.toSet()
            val labels = sectionLabels(sections)
            entry(
                index = index - if (current != null) 1 else 0,
                ids = history.map { it.first },
                records = records,
                lines = sections.flatMapIndexed { position, section ->
                    listOf(HistoryLine(label = labels[position], segments = emptyList())) +
                        section.verses.map { verse ->
                            HistoryLine(
                                label = null,
                                segments = listOf(
                                    DiffSegment(
                                        text = verse.text,
                                        changed =
                                        neighbour != null && verse.text !in others,
                                    ),
                                ),
                            )
                        }
                },
            )
        }
        return HistoryUiState(
            loading = false,
            scope = HistoryScope.SONG,
            title = document?.title.orEmpty(),
            entries = entries,
        )
    }

    // The entry of the version at [index] in the history, or of the text now for -1.
    private fun entry(index: Int, ids: List<String>, records: List<Record>, lines: List<HistoryLine>): HistoryEntry {
        val record = records.getOrNull(index)
        val now = Instant.now(clock)
        return HistoryEntry(
            versionId = ids.getOrNull(index),
            recorded = record?.let { relativeTime(it.at, now = now, zone = clock.zone) },
            time = record?.at?.atZone(clock.zone)?.toLocalTime(),
            source = record?.source,
            lines = lines,
        )
    }

    // The sections of a snapshot, as the editor reads a song: a repeat shows the verses of its source.
    private fun SongSnapshot.toSections(): List<SongSection> = sections.mapIndexed { index, section ->
        val source = section.repeatOf?.let { sections.getOrNull(it) } ?: section
        SongSection(
            id = index.toString(),
            kind = section.kind,
            name = section.name,
            repeatOf = section.repeatOf?.toString(),
            verses = source.lines.mapIndexed { position, text -> SongVerse(id = "$index-$position", text = text) },
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("songId") songId: String, @Assisted("verseId") verseId: String?): HistoryViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
