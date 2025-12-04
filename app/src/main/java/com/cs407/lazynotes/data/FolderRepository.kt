package com.cs407.lazynotes.data

import androidx.compose.runtime.mutableStateListOf

/**
 * A simple data class to represent a folder.
 */
data class Folder(
    val name: String,
    val lastModified: Long = System.currentTimeMillis() // tracks recent activity
)

/**
 * A singleton repository to manage folders throughout the app.
 */
object FolderRepository {

    // Start with some default folders for demonstration
    private val _folders = mutableStateListOf(
        Folder(name = "Math"),
        Folder(name = "CS")
    )

    // Public, read-only reference to the backing list (don’t mutate from outside)
    val folders: List<Folder> = _folders

    /**
     * Adds a new folder to the repository.
     * Returns true if added, false if invalid or duplicate.
     */
    fun addFolder(folderName: String): Boolean {
        if (folderName.isNotBlank() && _folders.none { it.name.equals(folderName, ignoreCase = true) }) {
            _folders.add(Folder(name = folderName, lastModified = System.currentTimeMillis()))
            return true
        }
        return false
    }

    /**
     * Update a folder's lastModified to now (e.g., when a note is added/edited).
     */
    fun touchFolder(folderName: String) {
        // Find index (we store folders in a mutableStateListOf; replace to trigger recomposition)
        val index = _folders.indexOfFirst { it.name.equals(folderName, ignoreCase = true) }
        if (index != -1) {
            val existing = _folders[index]
            _folders[index] = existing.copy(lastModified = System.currentTimeMillis())
        }
    }

    /**
     * Returns folders ordered per the current preference.
     * - Alphabetical when 'alphabetical' is true
     * - Recently Edited (by lastModified desc) otherwise
     */
    fun getFoldersOrdered(alphabetical: Boolean): List<Folder> {
        return if (alphabetical) {
            _folders.sortedBy { it.name.lowercase() }
        } else {
            _folders.sortedByDescending { it.lastModified }
        }
    }
}