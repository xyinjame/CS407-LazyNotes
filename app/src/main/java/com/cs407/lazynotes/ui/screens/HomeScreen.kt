package com.cs407.lazynotes.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cs407.lazynotes.data.FolderRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToNew: () -> Unit,
    onNavigateToViewNotes: (String) -> Unit // Modified to take folder name
) {
    // Observe the folders directly from the global repository
    val folders = FolderRepository.folders

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
    ) {
        Column(
            modifier = Modifier.padding(it).fillMaxSize()
        ) {
            if (folders.isEmpty()) {
                Text("No folders yet. Add one!", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(folders) { folder ->
                        ListItem(
                            headlineContent = { Text(folder.name) },
                            // Pass the folder name on click
                            modifier = Modifier.clickable { onNavigateToViewNotes(folder.name) }
                        )
                    }
                }
            }
        }
    }
}
