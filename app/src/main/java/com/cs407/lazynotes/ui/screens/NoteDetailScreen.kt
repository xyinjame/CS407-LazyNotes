package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cs407.lazynotes.R
import com.cs407.lazynotes.data.FolderRepository
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

    // Local state for "move to folder"
    var isMoving by remember { mutableStateOf(false) }
    val folders = FolderRepository.folders

    // Observe preference: true = show transcript, false = show summary
    val showTranscriptFirst by Preferences.showTranscriptFirst.collectAsState(initial = true)

    // Local toggle for this screen, initialized from preference
    var showTranscript by remember(noteId, showTranscriptFirst) {
        mutableStateOf(showTranscriptFirst)
    }

    val primary = colorResource(id = R.color.primary_blue)
    val accent = colorResource(id = R.color.accent_coral)
    val background = colorResource(id = R.color.background_light)
    val surface = colorResource(id = R.color.surface_white)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)

    LaunchedEffect(noteId) {
        if (noteId != null) {
            val found = NoteRepository.getNoteById(noteId)
            note = found
            editedTitle = found?.title.orEmpty()
        }
    }

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        note?.title ?: "Note Detail",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = primary
                        )
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
                        DropdownMenuItem(
                            text = { Text("Move to folder") },
                            onClick = {
                                menuExpanded = false
                                if (note != null) {
                                    isMoving = true
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (showTranscript)
                                        "Show summary view"
                                    else
                                        "Show transcript view"
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                showTranscript = !showTranscript
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = textPrimary,
                    navigationIconContentColor = primary
                ),
                modifier = Modifier.shadow(elevation = 2.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(background)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            note?.let { noteDetail ->
                // Choose which text to show
                val label = if (showTranscript) "Transcript" else "Summary"
                val content = if (showTranscript) noteDetail.transcript else noteDetail.summary

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = surface
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Single chosen section
                        Text(
                            label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            content ?: "No ${label.lowercase()} available.",
                            style = if (label == "Transcript") MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                            color = primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = surface
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Audio File",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(noteDetail.audioUri ?: "No audio file linked.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Audio File Section
                Text(noteDetail.audioUri ?: "No audio file linked.", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { content?.let { onGenerateFlashcards(it) } },
                    enabled = !content.isNullOrBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.White,
                        disabledContainerColor = accent.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Generate Flashcards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } ?: run {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = surface
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(32.dp)) {
                        Text(
                            "Note not found",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                    }
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

    // Dialog to move note to a different folder
    if (isMoving && note != null) {
        val current = note!!
        AlertDialog(
            onDismissRequest = { isMoving = false },
            title = { Text("Move note") },
            text = {
                Column {
                    Text("Choose a folder:")
                    Spacer(modifier = Modifier.height(8.dp))

                    folders.forEach { targetFolder ->

                        TextButton(
                            onClick = {
                                NoteRepository.moveNoteToFolder(current.id, targetFolder.name)
                                note = current.copy(folderName = targetFolder.name)
                                isMoving = false
                            }
                        ) {
                            Text(targetFolder.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isMoving = false }) {
                    Text("Close")
                }
            },
            dismissButton = {}
        )
    }
}