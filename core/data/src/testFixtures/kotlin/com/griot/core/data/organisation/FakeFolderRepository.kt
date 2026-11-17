package com.griot.core.data.organisation

import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [FolderRepository] for tests: [folders] is what it lists; [calls] records every change asked, such as
 * `"create my-name MAGENTA"`, in order.
 */
class FakeFolderRepository(folders: List<Folder> = emptyList()) : FolderRepository {
    val folders = MutableStateFlow(folders)

    val calls = mutableListOf<String>()

    override fun observeFolders() = folders

    override suspend fun createFolder(name: String, color: FolderColor): String {
        calls += "create $name $color"
        return "created-folder-id"
    }

    override suspend fun renameFolder(id: String, name: String) {
        calls += "rename $id $name"
    }

    override suspend fun setFolderColor(id: String, color: FolderColor) {
        calls += "color $id $color"
    }

    override suspend fun deleteFolder(id: String) {
        calls += "delete $id"
    }

    override suspend fun fileSongs(songIds: List<String>, add: Set<String>, remove: Set<String>) {
        calls += "file ${songIds.joinToString()} +${add.sorted().joinToString()} -${remove.sorted().joinToString()}"
    }
}
