package com.griot.core.data.organisation

import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import kotlinx.coroutines.flow.Flow

/** The folders of songs, one level only, synced like the songs. « Archives » is built in. */
interface FolderRepository {
    /** The folders in order, « Archives » last, each with its count of songs. */
    fun observeFolders(): Flow<List<Folder>>

    /** Creates a folder named [name] in [color] after the others, and returns its id. */
    suspend fun createFolder(name: String, color: FolderColor): String

    /** Renames the folder [id]; « Archives » keeps its name. */
    suspend fun renameFolder(id: String, name: String)

    /** Gives the folder [id] the [color]; « Archives » keeps its colour. */
    suspend fun setFolderColor(id: String, color: FolderColor)

    /** Deletes the folder [id], never its songs: they go back to « Aucun dossier ». « Archives » stays. */
    suspend fun deleteFolder(id: String)

    companion object {
        /** The id of « Archives », the same on every device. */
        const val ARCHIVES_ID = "archives"
    }
}
