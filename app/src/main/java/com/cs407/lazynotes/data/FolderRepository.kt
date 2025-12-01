package com.cs407.lazynotes.data

import androidx.compose.runtime.mutableStateListOf

/**
 * A simple data class to represent a folder.
 */
data class Folder(val name: String)

/**
 * A singleton repository to manage folders throughout the app.
 * This ensures all parts of the app are working with the same set of folders.
 */
object FolderRepository {

    // Start with some default folders for demonstration
    private val _folders = mutableStateListOf(
        Folder("CS"),
        Folder("Math")
    )

    /**
     * Public, read-only access to the list of folders.
     * Composables can observe this to react to changes.
     */
    val folders: List<Folder> = _folders

    /**
     * Adds a new folder to the repository.
     * It checks for duplicates (case-insensitive).
     * @param folderName The name of the folder to add.
     * @return True if the folder was added, false if it already exists.
     */
    fun addFolder(folderName: String): Boolean {
        if (folderName.isNotBlank() && _folders.none { it.name.equals(folderName, ignoreCase = true) }) {
            _folders.add(Folder(folderName))
            return true
        }
        return false
    }
}
