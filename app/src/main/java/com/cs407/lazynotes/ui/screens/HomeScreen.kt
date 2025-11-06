package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data classes for our app
data class Note(
    val id: String,
    val title: String
)

data class Folder(
    val id: String,
    val name: String,
    val notes: List<Note>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToViewNotes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNew: () -> Unit
) {
    // Sample data - this will be replaced with real data later
    val folders = remember {
        listOf(
            Folder(
                id = "1",
                name = "CS Notes",
                notes = listOf(
                    Note("1", "CS 407 Lecture 1 Notes"),
                    Note("2", "CS 407 Lecture 2 Notes")
                )
            ),
            Folder(
                id = "2",
                name = "MATH Notes",
                notes = listOf(
                    Note("3", "MATH Lecture 2 Notes")
                )
            )
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var expandedFolders by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LazyNotes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { onNavigateToSettings() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE0E0E0)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToNew() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Folders and Notes List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(folders) { folder ->
                    FolderItem(
                        folder = folder,
                        isExpanded = expandedFolders.contains(folder.id),
                        onToggleExpand = {
                            expandedFolders = if (expandedFolders.contains(folder.id)) {
                                expandedFolders - folder.id
                            } else {
                                expandedFolders + folder.id
                            }
                        },
                        onNoteClick = { onNavigateToViewNotes() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu"
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        ),
        singleLine = true
    )
}

@Composable
fun FolderItem(
    folder: Folder,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNoteClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Folder Header (the drawer)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() },
            color = Color(0xFFD0D0D0),
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = folder.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
        }

        // Notes inside the folder (shown when expanded)
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                folder.notes.forEach { note ->
                    NoteItem(
                        note = note,
                        onClick = {onNoteClick()}
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}


@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit
    ) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color(0xFFB0B0B0),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = note.title,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp
        )
    }
}