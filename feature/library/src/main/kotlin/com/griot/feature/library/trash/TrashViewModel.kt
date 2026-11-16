package com.griot.feature.library.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.model.TrashedSong
import com.griot.core.data.song.SongRepository
import com.griot.core.data.song.SongRepository.Companion.TRASH_DAYS
import com.griot.core.design.format.relativeTime
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
internal class TrashViewModel @Inject constructor(private val songs: SongRepository, private val clock: Clock) :
    ViewModel() {
    val uiState: StateFlow<TrashUiState> = songs.observeTrash()
        .map { trash ->
            val now = Instant.now(clock)
            TrashUiState(items = trash.map { it.toItem(now) }, loading = false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), TrashUiState())

    fun onRestore(id: String) {
        viewModelScope.launch { songs.restoreSong(id) }
    }

    private fun TrashedSong.toItem(now: Instant): TrashItem {
        val left = Duration.ofDays(TRASH_DAYS).minus(Duration.between(deletedAt, now))
        // Rounded up: a song with a few hours left still shows « 1 jour ».
        val daysLeft = (left.toHours() + HOURS_PER_DAY - 1) / HOURS_PER_DAY
        return TrashItem(
            song = this,
            deleted = relativeTime(deletedAt, now = now, zone = clock.zone),
            daysLeft = daysLeft.coerceAtLeast(1),
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val HOURS_PER_DAY = 24L
    }
}
