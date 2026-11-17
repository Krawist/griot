package com.griot.core.data.song

import com.griot.core.data.model.SongVersion
import com.griot.core.data.model.VerseVersion
import com.griot.core.data.model.VersionSource
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [VersionRepository] for tests: [verseHistory], [songHistory] and [conflicts] are what it lists; [calls]
 * records every change asked, such as `"record verse my-verse-id AUTHOR"`, in order.
 */
class FakeVersionRepository(
    verseHistory: List<VerseVersion> = emptyList(),
    songHistory: List<SongVersion> = emptyList(),
    conflicts: List<String> = emptyList(),
) : VersionRepository {
    val verseHistory = MutableStateFlow(verseHistory)

    val songHistory = MutableStateFlow(songHistory)

    val conflicts = MutableStateFlow(conflicts)

    val calls = mutableListOf<String>()

    override fun observeVerseHistory(verseId: String) = verseHistory

    override fun observeSongHistory(songId: String) = songHistory

    override fun observeConflicts(songId: String) = conflicts

    override suspend fun dismissVerseConflicts(verseId: String) {
        calls += "dismiss verse conflicts $verseId"
    }

    override suspend fun dismissSongConflicts(songId: String) {
        calls += "dismiss song conflicts $songId"
    }

    override suspend fun recordVerse(verseId: String, source: VersionSource) {
        calls += "record verse $verseId $source"
    }

    override suspend fun recordSong(songId: String) {
        calls += "record song $songId"
    }

    override suspend fun restoreVerse(versionId: String) {
        calls += "restore verse $versionId"
    }

    override suspend fun restoreSong(versionId: String) {
        calls += "restore song $versionId"
    }

    override suspend fun thin() {
        calls += "thin"
    }
}
