package com.cs407.lazynotes.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * A rich data class representing a single note with all its content.
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val folderName: String, // The name of the folder this note belongs to
    val summary: String?,
    val transcript: String?,
    val audioUri: String? // The URI of the locally saved audio file
)

/**
 * A singleton repository to manage notes throughout the app.
 */
object NoteRepository {

    private val _notes = mutableStateListOf<Note>()

    /**
     * Public, read-only access to the list of all notes.
     */
    val notes: List<Note> = _notes

    private var currentUserId: Int? = null
    private lateinit var database: AppDataDatabase

    fun initialize(context: Context, userId: Int) {
        currentUserId = userId
        database = AppDataDatabase.getDatabase(context)
        loadNotes()
    }

    private fun loadNotes() {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = currentUserId ?: return@launch
            val allNoteEntities = mutableListOf<NoteEntity>()

            val folders = database.folderDao().getFoldersByUser(userId)
            folders.forEach { folder ->
                val folderNotes = database.noteDao().getNotesForFolder(userId, folder.name)
                allNoteEntities.addAll(folderNotes)
            }

            withContext(Dispatchers.Main) {
                _notes.clear()
                _notes.addAll(allNoteEntities.map {
                    Note(
                        id = it.id,
                        title = it.title,
                        folderName = it.folderName,
                        summary = it.summary,
                        transcript = it.transcript,
                        audioUri = it.audioUri
                    )
                })
            }
        }
    }

    /**
     * Adds a new note to the repository.
     */
    fun addNote(note: Note) {
        val userId = currentUserId ?: return
        _notes.add(note)
        FolderRepository.touchFolder(note.folderName)

        CoroutineScope(Dispatchers.IO).launch {
            database.noteDao().insert(
                NoteEntity(
                    id = note.id,
                    userId = userId,
                    title = note.title,
                    folderName = note.folderName,
                    summary = note.summary,
                    transcript = note.transcript,
                    audioUri = note.audioUri
                )
            )
        }

        println("Note '${note.title}' added to folder '${note.folderName}'. Total notes: ${_notes.size}")
    }

    /**
     * Call this if you implement editing a note's content.
     * Touches the folder to mark as recently edited.
     */
    fun updateNote(updated: Note) {
        val userId = currentUserId ?: return
        val index = _notes.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            val old = _notes[index]
            _notes[index] = updated
            // If moved folders, touch both
            if (!old.folderName.equals(updated.folderName, ignoreCase = true)) {
                FolderRepository.touchFolder(old.folderName)
            }
            FolderRepository.touchFolder(updated.folderName)

            CoroutineScope(Dispatchers.IO).launch {
                database.noteDao().update(
                    NoteEntity(
                        id = updated.id,
                        userId = userId,
                        title = updated.title,
                        folderName = updated.folderName,
                        summary = updated.summary,
                        transcript = updated.transcript,
                        audioUri = updated.audioUri
                    )
                )
            }

        }
    }

    /**
     * Gets all notes that belong to a specific folder.
     */
    fun getNotesForFolder(folderName: String): List<Note> {
        return _notes.filter { it.folderName.equals(folderName, ignoreCase = true) }
    }

    /**
     * Gets notes for a folder in a defined order (alphabetical by title for now).
     */
    fun getNotesForFolderOrdered(folderName: String, alphabeticalByTitle: Boolean = true): List<Note> {
        val list = getNotesForFolder(folderName)
        return if (alphabeticalByTitle) {
            list.sortedBy { it.title.lowercase() }
        } else {
            list.toList()
        }
    }

    /**
     * Finds a single note by its unique ID.
     */
    fun getNoteById(noteId: String): Note? {
        return _notes.find { it.id == noteId }
    }

    /**
     * Update note title.
     */
    fun updateNoteTitle(noteId: String, newTitle: String) {
        val userId = currentUserId ?: return
        val index = _notes.indexOfFirst { it.id == noteId }
        if (index != -1) {
            val old = _notes[index]
            val updated = old.copy(title = newTitle)
            _notes[index] = updated

            CoroutineScope(Dispatchers.IO).launch {
                database.noteDao().update(
                    NoteEntity(
                        id = updated.id,
                        userId = userId,
                        title = updated.title,
                        folderName = updated.folderName,
                        summary = updated.summary,
                        transcript = updated.transcript,
                        audioUri = updated.audioUri
                    )
                )
            }
        }
    }

    /**
     * Delete note.
     */
    fun deleteNote(noteId: String) {
        val removed = _notes.firstOrNull { it.id == noteId }
        if (removed != null) {
            _notes.remove(removed)

            // Mark the folder as recently edited
            FolderRepository.touchFolder(removed.folderName)

            CoroutineScope(Dispatchers.IO).launch {
                database.noteDao().delete(noteId)
            }
        }
    }

    /**
     * Move note to another folder.
     */
    fun moveNoteToFolder(noteId: String, newFolderName: String) {
        val userId = currentUserId ?: return
        val index = _notes.indexOfFirst { it.id == noteId }
        if (index != -1) {
            val old = _notes[index]
            val updated = old.copy(folderName = newFolderName)
            _notes[index] = updated

            // Mark the folder as recently edited
            FolderRepository.touchFolder(old.folderName)
            FolderRepository.touchFolder(newFolderName)

            CoroutineScope(Dispatchers.IO).launch {
                database.noteDao().update(
                    NoteEntity(
                        id = updated.id,
                        userId = userId,
                        title = updated.title,
                        folderName = updated.folderName,
                        summary = updated.summary,
                        transcript = updated.transcript,
                        audioUri = updated.audioUri
                    )
                )
            }
        }
    }

    /**
     * Helper for adjusting notes after folder rename.
     */
    fun renameNotesForFolder(oldFolderName: String, newFolderName: String) {
        val userId = currentUserId ?: return

        _notes.forEachIndexed { index, note ->
            if (note.folderName.equals(oldFolderName, ignoreCase = true)) {
                val updated = note.copy(folderName = newFolderName)
                _notes[index] = updated

                CoroutineScope(Dispatchers.IO).launch {
                    database.noteDao().update(
                        NoteEntity(
                            id = updated.id,
                            userId = userId,
                            title = updated.title,
                            folderName = updated.folderName,
                            summary = updated.summary,
                            transcript = updated.transcript,
                            audioUri = updated.audioUri
                        )
                    )
                }
            }
        }

        // Mark the folder as recently edited
        FolderRepository.touchFolder(newFolderName)
    }

    /**
     * Delete all notes under a given folder.
     */
    fun deleteNotesInFolder(folderName: String) {
        val userId = currentUserId ?: return

        _notes.removeAll { it.folderName.equals(folderName, ignoreCase = true) }

        CoroutineScope(Dispatchers.IO).launch {
            database.noteDao().deleteNotesForFolder(userId, folderName)
        }
    }

    fun clear() {
        _notes.clear()
        currentUserId = null
    }
}