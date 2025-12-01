package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelectScreen(
    navController: NavController,
    onNavigateToHome: () -> Unit,
    onNavigateToNewFolder: () -> Unit,
    clientRefId: String?,
    audioUri: String?,
    viewModel: FolderSelectViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val vmNoteTitle by viewModel.noteTitle.collectAsState()
    var noteTitle by remember { mutableStateOf("") }

    val folders = viewModel.folders

    // The effect now triggers polling and passes all necessary data to the ViewModel
    LaunchedEffect(clientRefId, audioUri) {
        if (clientRefId != null) {
            viewModel.startPolling(clientRefId, audioUri)
        }
    }

    LaunchedEffect(vmNoteTitle) {
        noteTitle = vmNoteTitle
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Folder") },
                navigationIcon = { IconButton(onClick = onNavigateToHome) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = { IconButton(onClick = onNavigateToHome) { Icon(Icons.Default.Home, "Home") } }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it).fillMaxSize()) {
            when (val state = uiState) {
                is PollingUiState.Polling -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text("Processing Transcription...", modifier = Modifier.padding(top = 16.dp))
                        }
                    }
                }
                is PollingUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Transcription Complete!",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            TextField(
                                value = noteTitle,
                                onValueChange = {
                                    noteTitle = it
                                    viewModel.updateNoteTitle(it)
                                },
                                label = { Text("Note Title") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            )

                            Text("Save to Folder", style = MaterialTheme.typography.titleMedium)
                        }
                        
                        HorizontalDivider()

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(folders) { folder ->
                                ListItem(
                                    headlineContent = { Text(folder.name) },
                                    modifier = Modifier.clickable {
                                        viewModel.saveNoteToFolder(folder.name)
                                        onNavigateToHome()
                                    }
                                )
                            }
                        }
                        
                        HorizontalDivider()

                        Button(
                            onClick = onNavigateToNewFolder,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Folder", modifier = Modifier.padding(end = 8.dp))
                            Text("Create New Folder")
                        }
                    }
                }
                is PollingUiState.Error -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("Error: ${state.message}")
                    }
                }
                is PollingUiState.Timeout -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("Polling timed out. Please try again.")
                    }
                }
                is PollingUiState.Idle -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("No transcription in progress.")
                    }
                }
            }
        }
    }
}
