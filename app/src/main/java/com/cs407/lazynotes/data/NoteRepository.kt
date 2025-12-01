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
        println("Note '${note.title}' added to folder '${note.folderName}'. Total notes: ${_notes.size}")
    }

    /**
     * Gets all notes that belong to a specific folder.
     * @param folderName The name of the folder.
     * @return A list of notes for the given folder.
     */
    fun getNotesForFolder(folderName: String): List<Note> {
        return _notes.filter { it.folderName.equals(folderName, ignoreCase = true) }
    }

    /**
     * Finds a single note by its unique ID.
     * @param noteId The ID of the note to find.
     * @return The Note object if found, otherwise null.
     */
    fun getNoteById(noteId: String): Note? {
        return _notes.find { it.id == noteId }
    }
}
