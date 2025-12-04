package com.cs407.lazynotes.data

import androidx.compose.runtime.mutableStateListOf
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

    /**
     * Adds a new note to the repository.
     */
    fun addNote(note: Note) {
        _notes.add(note)
        // Mark the folder as recently modified
        FolderRepository.touchFolder(note.folderName)
        println("Note '${note.title}' added to folder '${note.folderName}'. Total notes: ${_notes.size}")
    }

    /**
     * Call this if you implement editing a note's content.
     * Touches the folder to mark as recently edited.
     */
    fun updateNote(updated: Note) {
        val index = _notes.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            val old = _notes[index]
            _notes[index] = updated
            // If moved folders, touch both
            if (!old.folderName.equals(updated.folderName, ignoreCase = true)) {
                FolderRepository.touchFolder(old.folderName)
            }
            FolderRepository.touchFolder(updated.folderName)
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
        val index = _notes.indexOfFirst { it.id == noteId }
        if (index != -1) {
            val old = _notes[index]
            _notes[index] = old.copy(title = newTitle)
        }
    }

    /**
     * Delete note.
     */
    fun deleteNote(noteId: String) {
        _notes.removeAll { it.id == noteId }
    }

    /**
     * Move note to another folder.
     */
    fun moveNoteToFolder(noteId: String, newFolderName: String) {
        val index = _notes.indexOfFirst { it.id == noteId }
        if (index != -1) {
            val old = _notes[index]
            _notes[index] = old.copy(folderName = newFolderName)
        }
    }
}