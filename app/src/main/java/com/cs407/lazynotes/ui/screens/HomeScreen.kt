package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.cs407.lazynotes.data.FolderRepository
import com.cs407.lazynotes.data.NoteRepository
import com.cs407.lazynotes.data.Preferences
import com.cs407.lazynotes.data.Folder
import com.cs407.lazynotes.data.Note

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LazyNotes") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNew) {
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
                Text("No folders yet. Add one!", modifier = Modifier.padding(16.dp))
            } else {
                // List with per-folder expand / collapse
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(
                        items = folders,
                        key = { it.name.lowercase() }
                    ) { folder ->
                        // Each folder remembers whether it is expanded.
                        // The preference controls only the default state.
                        var isExpanded by rememberSaveable(folder.name, openByDefault) {
                            mutableStateOf(openByDefault)
                        }

                        // Folder header row with name and dropdown arrow
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = folder.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.clickable {
                                        // Tapping the name opens the folder view
                                        onNavigateToViewNotes(folder.name)
                                    }
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
                                        }
                                    )
                                }
                            },
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
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
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
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    notes.forEachIndexed { index, note ->
                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    text = note.title,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            },
                                            modifier = Modifier
                                                .clickable { onNoteClick(note.id) }
                                                .padding(vertical = 4.dp)
                                        )
                                        if (index < notes.lastIndex) {
                                            Divider(
                                                color = Color.Black.copy(alpha = 0.06f)
                                            )
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
                }
            }
        }
    }
}