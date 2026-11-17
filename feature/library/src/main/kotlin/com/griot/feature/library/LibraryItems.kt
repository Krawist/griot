package com.griot.feature.library

import com.griot.core.data.model.Folder
import com.griot.core.data.model.Genre
import com.griot.core.data.model.LibrarySong
import java.time.Instant

/** Builds the cards of songs, shared by the home screen and the folder screen. */
internal interface LibraryItems {
    /** The card of [song] at [now]: its time, its rhyme dots, its folder among [folders] and genre among [genres]. */
    fun itemOf(song: LibrarySong, folders: List<Folder>, genres: List<Genre>, now: Instant): LibraryItem
}
