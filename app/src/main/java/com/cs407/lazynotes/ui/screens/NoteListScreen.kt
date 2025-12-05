package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.saveable.rememberSaveable
import com.cs407.lazynotes.data.FolderRepository
import com.cs407.lazynotes.data.NoteRepository
import com.cs407.lazynotes.R

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
    // Use a local state for the current folder name so it can update after rename
    val initialFolderName = folderName ?: "Notes"
    var currentFolderName by rememberSaveable { mutableStateOf(initialFolderName) }

    // Get the notes for the specific folder from the global repository
    val notes = NoteRepository.getNotesForFolder(currentFolderName)

    // State for folder rename menu and dialog
    var folderMenuExpanded by remember { mutableStateOf(false) }
    var isRenamingFolder by remember { mutableStateOf(false) }
    var editedFolderName by remember { mutableStateOf(currentFolderName) }

    val primary = colorResource(id = R.color.primary_blue)
    val background = colorResource(id = R.color.background_light)
    val surface = colorResource(id = R.color.surface_white)
    val folderBackground = colorResource(id = R.color.folder_background)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)
    val dividerColor = colorResource(id = R.color.divider_color)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentFolderName,
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
                    // Menu for folder actions
                    IconButton(onClick = { folderMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Folder options",
                            tint = primary
                        )
                    }
                    DropdownMenu(
                        expanded = folderMenuExpanded,
                        onDismissRequest = { folderMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename Folder") },
                            onClick = {
                                folderMenuExpanded = false
                                editedFolderName = currentFolderName
                                isRenamingFolder = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = primary,
                    navigationIconContentColor = primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(background)
        ) {
            if (notes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
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
                                "No notes in this folder yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 2.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = surface
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        LazyColumn {
                            items(notes) { note ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            note.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            note.summary ?: "No summary available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = textSecondary, 
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    },
                                    colors = androidx.compose.material3.ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .clickable { onNoteClick(note.id) }
                                        .padding(vertical = 4.dp)
                                )
                                if (note != notes.last()) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = dividerColor,
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog to rename the folder
    if (isRenamingFolder) {
        AlertDialog(
            onDismissRequest = { isRenamingFolder = false },
            title = { Text("Rename folder") },
            text = {
                TextField(
                    value = editedFolderName,
                    onValueChange = { editedFolderName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = editedFolderName.trim()
                        if (trimmed.isNotEmpty() && trimmed != currentFolderName) {
                            val renamed = FolderRepository.renameFolder(currentFolderName, trimmed)
                            if (renamed) {
                                // Keep notes within the folder in sync
                                NoteRepository.renameNotesForFolder(currentFolderName, trimmed)
                                currentFolderName = trimmed
                            }
                        }
                        isRenamingFolder = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isRenamingFolder = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
