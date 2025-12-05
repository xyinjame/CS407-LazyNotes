package com.cs407.lazynotes.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val _folders = mutableStateListOf<Folder>()

    // Public, read-only reference to the backing list (don’t mutate from outside)
    val folders: List<Folder> = _folders

    private var currentUserId: Int? = null
    private lateinit var database: AppDataDatabase

    fun initialize(context: Context, userId: Int) {
        currentUserId = userId
        database = AppDataDatabase.getDatabase(context)
        loadFolders()
    }

    private fun loadFolders() {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = currentUserId ?: return@launch
            val folderEntities = database.folderDao().getFoldersByUser(userId)
            withContext(Dispatchers.Main) {
                _folders.clear()
                _folders.addAll(folderEntities.map {
                    Folder(name = it.name, lastModified = it.lastModified)
                })
            }
        }
    }


    /**
     * Adds a new folder to the repository.
     * Returns true if added, false if invalid or duplicate.
     */
    fun addFolder(folderName: String): Boolean {
        if (folderName.isNotBlank() && _folders.none { it.name.equals(folderName, ignoreCase = true) }) {
            val userId = currentUserId ?: return false
            val folder = Folder(name = folderName, lastModified = System.currentTimeMillis())
            _folders.add(folder)

            CoroutineScope(Dispatchers.IO).launch {
                database.folderDao().insert(
                    FolderEntity(
                        userId = userId,
                        name = folderName,
                        lastModified = folder.lastModified
                    )
                )
            }

            return true
        }
        return false
    }

    /**
     * Update a folder's lastModified to now (e.g., when a note is added/edited).
     */
    fun touchFolder(folderName: String) {
        // Find index (we store folders in a mutableStateListOf; replace to trigger recomposition)
        val userId = currentUserId ?: return
        val index = _folders.indexOfFirst { it.name.equals(folderName, ignoreCase = true) }
        if (index != -1) {
            val timestamp = System.currentTimeMillis()
            val existing = _folders[index]
            _folders[index] = existing.copy(lastModified = System.currentTimeMillis())

            CoroutineScope(Dispatchers.IO).launch {
                database.folderDao().updateLastModified(userId, folderName, timestamp)
            }
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

    fun clear() {
        _folders.clear()
        currentUserId = null
    }
}