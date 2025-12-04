package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cs407.lazynotes.data.NoteRepository
import com.cs407.lazynotes.data.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String?,
    onNavigateBack: () -> Unit,
    onGenerateFlashcards: (String) -> Unit
) {
    var note by remember { mutableStateOf<com.cs407.lazynotes.data.Note?>(null) }

    // Local state for rename menu + dialog
    var menuExpanded by remember { mutableStateOf(false) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf("") }

    // Local state for delete confirmation
    var isDeleting by remember { mutableStateOf(false) }

    // Observe preference: true = show transcript, false = show summary
    val showTranscriptFirst by Preferences.showTranscriptFirst.collectAsState(initial = true)

    LaunchedEffect(noteId) {
        if (noteId != null) {
            val found = NoteRepository.getNoteById(noteId)
            note = found
            editedTitle = found?.title.orEmpty()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(note?.title ?: "Note Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Menu for actions like Rename, Delete, Move
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = {
                                menuExpanded = false
                                note?.let {
                                    editedTitle = it.title
                                    isEditingTitle = true
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuExpanded = false
                                if (note != null) {
                                    isDeleting = true
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            note?.let { noteDetail ->
                // Choose which text to show
                val label = if (showTranscriptFirst) "Transcript" else "Summary"
                val content = if (showTranscriptFirst) noteDetail.transcript else noteDetail.summary

                // Single chosen section
                Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    content ?: "No ${label.lowercase()} available.",
                    style = if (label == "Transcript") MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Audio File Section
                Text("Audio File", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(noteDetail.audioUri ?: "No audio file linked.", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(24.dp))

                // Button to generate flashcards: only uses the chosen content.
                Button(
                    onClick = { content?.let { onGenerateFlashcards(it) } },
                    enabled = !content.isNullOrBlank()
                ) {
                    Text("Generate Flashcards")
                }
            }
        }
    }

    // Dialog to edit the note title
    if (isEditingTitle && note != null) {
        AlertDialog(
            onDismissRequest = { isEditingTitle = false },
            title = { Text("Edit title") },
            text = {
                TextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = editedTitle.trim()
                        if (trimmed.isNotEmpty()) {
                            val current = note!!
                            // Update repository
                            NoteRepository.updateNoteTitle(current.id, trimmed)
                            // Update local state so UI refreshes immediately
                            note = current.copy(title = trimmed)
                        }
                        isEditingTitle = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingTitle = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    // Dialog to confirm delete
    if (isDeleting && note != null) {
        AlertDialog(
            onDismissRequest = { isDeleting = false },
            title = { Text("Delete note") },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val current = note!!
                        NoteRepository.deleteNote(current.id)
                        isDeleting = false
                        onNavigateBack() // go back after deleting
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleting = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}