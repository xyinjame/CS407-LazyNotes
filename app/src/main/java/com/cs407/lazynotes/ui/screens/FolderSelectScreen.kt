package com.cs407.lazynotes.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs407.lazynotes.R
import com.cs407.lazynotes.ui.theme.MainBackground
import com.cs407.lazynotes.ui.theme.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelectScreen(
    navController: NavController,
    onNavigateToHome: () -> Unit,
    onNavigateToNewFolder: () -> Unit,
    clientRefId: String?,
    viewModel: FolderSelectViewModel
) {
    val pollingState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // This effect will start polling when the screen is first composed.
    LaunchedEffect(clientRefId) {
        if (clientRefId != null) {
            viewModel.startPolling(clientRefId)
        } else {
            // Handle the case where clientRefId is null, which is an error state.
            Toast.makeText(context, "Error: Missing transcription reference.", Toast.LENGTH_LONG).show()
            onNavigateToHome()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_folder), fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = { IconButton(onClick = onNavigateToHome) { Icon(Icons.Default.Home, "Home") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TopBar)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MainBackground),
            contentAlignment = Alignment.Center
        ) {
            when (val state = pollingState) {
                is PollingUiState.Idle, is PollingUiState.Polling -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Transcription in progress... Please wait.")
                    }
                }
                is PollingUiState.Success -> {
                    // Once successful, show the folder list.
                    val noteTitle = clientRefId?.substringBeforeLast('.') ?: "Transcription"
                    FolderSelectionContent(
                        noteTitle = noteTitle,
                        onNavigateToNewFolder = onNavigateToNewFolder,
                        onFolderSelected = {
                            viewModel.saveNoteToFolder(noteTitle, it)
                            Toast.makeText(context, "Note saved to $it!", Toast.LENGTH_SHORT).show()
                            onNavigateToHome()
                        }
                    )
                }
                is PollingUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is PollingUiState.Timeout -> {
                    Text("Polling timed out. Please try again.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun FolderSelectionContent(
    noteTitle: String,
    onNavigateToNewFolder: () -> Unit,
    onFolderSelected: (String) -> Unit
) {
    val folderNames = remember { mutableStateListOf("General", "Meetings", "Ideas") } // Dummy data

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Transcription complete!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Select a folder to save the note \"$noteTitle\".")
        Spacer(modifier = Modifier.height(24.dp))

        // New folder button
        SelectionCard(
            title = stringResource(id = R.string.new_folder),
            onClick = { onNavigateToNewFolder() } // Note: This doesn't handle saving the note yet.
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lazy column for existing folders
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(folderNames) { name ->
                FolderCard(
                    folderName = name,
                    onClick = { onFolderSelected(name) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FolderCard(folderName: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color.LightGray,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(text = folderName, modifier = Modifier.padding(16.dp), fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
