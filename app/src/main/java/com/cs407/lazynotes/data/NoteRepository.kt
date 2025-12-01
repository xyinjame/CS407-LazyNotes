package com.cs407.lazynotes.data

import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

/**
 * A simple data class to represent a note.
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val folder: String // The name of the folder this note belongs to
)

/**
 * A singleton repository to manage notes throughout the app.
 * This ensures all parts of the app are working with the same set of notes.
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
        println("Note '${note.title}' added to repository. Total notes: ${_notes.size}")
    }

    /**
     * Gets all notes that belong to a specific folder.
     * @param folderName The name of the folder.
     * @return A list of notes for the given folder.
     */
    fun getNotesForFolder(folderName: String): List<Note> {
        return _notes.filter { it.folder.equals(folderName, ignoreCase = true) }
    }
}
