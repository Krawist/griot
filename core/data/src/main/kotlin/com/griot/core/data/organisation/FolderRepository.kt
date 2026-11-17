package com.griot.core.data.organisation

import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import kotlinx.coroutines.flow.Flow

/** The folders of songs, one level only, synced like the songs. « Archives » is built in. */
interface FolderRepository {
    /** The folders in order, « Archives » last, each with its count of songs. */
    fun observeFolders(): Flow<List<Folder>>

    /**
     * Creates a folder named [name] after the others, and returns its id. Its colour is the next of the accents in
     * turn (magenta, turquoise, orange, yellow), so that the folders tell apart.
     */
    suspend fun createFolder(name: String): String

    /** Renames the folder [id]; « Archives » keeps its name. */
    suspend fun renameFolder(id: String, name: String)

    /** Gives the folder [id] the [color]; « Archives » keeps its colour. */
    suspend fun setFolderColor(id: String, color: FolderColor)

    /** Deletes the folder [id], never its songs: they only leave it. « Archives » stays. */
    suspend fun deleteFolder(id: String)

    /** Files the songs [songIds] in the folders [add], and takes them out of the folders [remove]; the others stay. */
    suspend fun fileSongs(songIds: List<String>, add: Set<String>, remove: Set<String>)

    companion object {
        /** The id of « Archives », the same on every device. */
        const val ARCHIVES_ID = "archives"
    }
}
