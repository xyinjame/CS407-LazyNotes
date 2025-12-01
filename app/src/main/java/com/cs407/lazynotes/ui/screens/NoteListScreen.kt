package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cs407.lazynotes.data.NoteRepository

/**
 * This screen now functions as a list of notes within a specific folder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    folderName: String?,
    onNoteClick: (String) -> Unit, // Callback to navigate to detail view
    onNavigateBack: () -> Unit
) {
    // Get the notes for the specific folder from the global repository
    val notes = remember(folderName) {
        if (folderName != null) {
            NoteRepository.getNotesForFolder(folderName)
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderName ?: "Notes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it).fillMaxSize()
        ) {
            if (notes.isEmpty()) {
                Text("No notes in this folder yet.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    items(notes) { note ->
                        ListItem(
                            headlineContent = { Text(note.title) },
                            supportingContent = { Text(note.summary ?: "", maxLines = 2) },
                            modifier = Modifier.clickable { onNoteClick(note.id) } // Navigate with Note ID
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
