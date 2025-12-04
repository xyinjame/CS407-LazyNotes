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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.cs407.lazynotes.data.FolderRepository
import com.cs407.lazynotes.data.NoteRepository
import com.cs407.lazynotes.data.Preferences
import com.cs407.lazynotes.data.Folder
import com.cs407.lazynotes.data.Note
import com.cs407.lazynotes.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToNew: () -> Unit,
    onNavigateToViewNotes: (String) -> Unit,
    onNoteClick: (String) -> Unit
) {
    // Observe preferences
    val alphabetical by Preferences.folderSortAlphabetical.collectAsState(initial = false)
    val openByDefault by Preferences.folderDefaultOpen.collectAsState(initial = false)

    // Obtain ordered folders
    val folders: List<Folder> = FolderRepository.getFoldersOrdered(alphabetical)

    val primary = colorResource(id = R.color.primary_blue)
    val secondary = colorResource(id = R.color.secondary_teal)
    val accent = colorResource(id = R.color.accent_coral)
    val backgroundLight = colorResource(id = R.color.background_light)
    val surface = colorResource(id = R.color.surface_white)
    val folderBackground = colorResource(id = R.color.folder_background)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)
    val dividerColor = colorResource(id = R.color.divider_color)

    Scaffold(
        containerColor = backgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "LazyNotes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = textPrimary,
                    actionIconContentColor = primary
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNew,
                containerColor = accent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Note or Folder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (folders.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No folders yet. Add one!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap the + button to create your first folder and start organizing your notes!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // List with per-folder expand / collapse
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 12.dp)
                ) {
                    items(
                        items = folders,
                        key = { it.name.lowercase() }
                    ) { folder ->
                        // Each folder remembers whether it is expanded.
                        // The preference controls only the default state.
                        var isExpanded by rememberSaveable(folder.name, openByDefault) {
                            mutableStateOf(openByDefault)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 2.dp,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = surface
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ) {
                            Column {
                                // Folder header row with name and dropdown arrow
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = folder.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = primary,
                                            modifier = Modifier.clickable {
                                                // Tapping the name opens the folder view
                                                onNavigateToViewNotes(folder.name)
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { isExpanded = !isExpanded }) {
                                            Icon(
                                                imageVector = if (isExpanded) {
                                                    Icons.Filled.KeyboardArrowUp
                                                } else {
                                                    Icons.Filled.KeyboardArrowDown
                                                },
                                                contentDescription = if (isExpanded) {
                                                    "Collapse folder"
                                                } else {
                                                    "Expand folder"
                                                },
                                                tint = secondary
                                            )
                                        }
                                    },
                                    colors = androidx.compose.material3.ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .padding(top = 6.dp, bottom = 4.dp)
                                )

                                if (isExpanded) {
                                    // Notes under this folder
                                    val notes: List<Note> =
                                        NoteRepository.getNotesForFolderOrdered(
                                            folder.name,
                                            alphabeticalByTitle = true
                                        )

                                    if (notes.isEmpty()) {
                                        // Subtle "empty" message, indented to align with notes block
                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    text = "No notes in this folder",
                                                    color = textSecondary,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            colors = androidx.compose.material3.ListItemDefaults.colors(
                                                containerColor = Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
                                                .padding(start = 8.dp) // extra indent
                                        )
                                    } else {
                                        // Notes container: indented with subtle background
                                        Column(
                                            modifier = Modifier
                                                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                                                .padding(start = 8.dp) // extra indent vs folder
                                                .background(
                                                    color = folderBackground,
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                                        10.dp
                                                    )
                                                )
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            notes.forEachIndexed { index, note ->
                                                // Menu + rename/delete state
                                                var noteMenuExpanded by remember { mutableStateOf(false) }
                                                var isRenaming by remember { mutableStateOf(false) }
                                                var isDeleting by remember { mutableStateOf(false) }
                                                var isMoving by remember { mutableStateOf(false) }
                                                var editedTitle by remember { mutableStateOf(note.title) }

                                                ListItem(
                                                    headlineContent = {
                                                        Text(
                                                            text = note.title,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = textPrimary,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    },
                                                    trailingContent = {
                                                        // Menu for this note
                                                        IconButton(onClick = { noteMenuExpanded = true }) {
                                                            Icon(
                                                                imageVector = Icons.Default.MoreVert,
                                                                contentDescription = "Note options"
                                                            )
                                                        }
                                                        DropdownMenu(
                                                            expanded = noteMenuExpanded,
                                                            onDismissRequest = { noteMenuExpanded = false }
                                                        ) {
                                                            DropdownMenuItem(
                                                                text = { Text("Rename") },
                                                                onClick = {
                                                                    noteMenuExpanded = false
                                                                    editedTitle = note.title
                                                                    isRenaming = true
                                                                }
                                                            )
                                                            DropdownMenuItem(
                                                                text = { Text("Delete") },
                                                                onClick = {
                                                                    noteMenuExpanded = false
                                                                    isDeleting = true
                                                                }
                                                            )
                                                            DropdownMenuItem(
                                                                text = { Text("Move to folder") },
                                                                onClick = {
                                                                    noteMenuExpanded = false
                                                                    isMoving = true
                                                                }
                                                            )
                                                        }
                                                    },
                                                    // No supportingContent: do not show summary/preview
                                                    modifier = Modifier
                                                        .clickable { onNoteClick(note.id) }
                                                        .padding(vertical = 4.dp)
                                                )

                                                // Rename dialog for this note
                                                if (isRenaming) {
                                                    AlertDialog(
                                                        onDismissRequest = { isRenaming = false },
                                                        title = { Text("Rename note") },
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
                                                                        NoteRepository.updateNoteTitle(
                                                                            note.id,
                                                                            trimmed
                                                                        )
                                                                    }
                                                                    isRenaming = false
                                                                }
                                                            ) {
                                                                Text("Save")
                                                            }
                                                        },
                                                        dismissButton = {
                                                            TextButton(onClick = { isRenaming = false }) {
                                                                Text("Cancel")
                                                            }
                                                        }
                                                    )
                                                }

                                                // Delete confirmation dialog for this note
                                                if (isDeleting) {
                                                    AlertDialog(
                                                        onDismissRequest = { isDeleting = false },
                                                        title = { Text("Delete note") },
                                                        text = {
                                                            Text("Are you sure you want to delete this note? This action cannot be undone.")
                                                        },
                                                        confirmButton = {
                                                            TextButton(
                                                                onClick = {
                                                                    NoteRepository.deleteNote(note.id)
                                                                    isDeleting = false
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

                                                // Move-to-folder dialog for this note
                                                if (isMoving) {
                                                    AlertDialog(
                                                        onDismissRequest = { isMoving = false },
                                                        title = { Text("Move note") },
                                                        text = {
                                                            Column {
                                                                Text("Choose a folder:")
                                                                Spacer(modifier = Modifier.height(8.dp))

                                                                // List all folders as options; you can skip the current one if you want
                                                                folders.forEach { targetFolder ->

                                                                    TextButton(
                                                                        onClick = {
                                                                            NoteRepository.moveNoteToFolder(note.id, targetFolder.name)
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

                                                if (index < notes.lastIndex) {
                                                    Divider(
                                                        modifier = Modifier.padding(horizontal = 12.dp),
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

                        // Group divider between folders
                        Divider(
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                            color = Color.Black.copy(alpha = 0.08f)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}