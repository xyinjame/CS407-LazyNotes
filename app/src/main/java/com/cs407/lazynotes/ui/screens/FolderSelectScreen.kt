package com.cs407.lazynotes.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cs407.lazynotes.R

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

    val primary = colorResource(id = R.color.primary_blue)
    val secondary = colorResource(id = R.color.secondary_teal)
    val accent = colorResource(id = R.color.accent_coral)
    val background = colorResource(id = R.color.background_light)
    val surface = colorResource(id = R.color.surface_white)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)

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
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Folder",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(
                            Icons.Default.Home,
                            "Home",
                            tint = primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = textPrimary,
                    navigationIconContentColor = primary,
                    actionIconContentColor = primary
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
        ) {
            when (val state = uiState) {
                is PollingUiState.Polling -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = surface
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                CircularProgressIndicator(color = secondary)

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    "Processing Transcription...",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "This may take a few minutes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }

                is PollingUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Transcription Complete!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = noteTitle,
                                onValueChange = {
                                    noteTitle = it
                                    viewModel.updateNoteTitle(it)
                                },
                                label = {
                                    Text(
                                        "Note Title",
                                        color = textSecondary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primary,
                                    unfocusedBorderColor = textSecondary,
                                    focusedLabelColor = primary,
                                    unfocusedLabelColor = textSecondary,
                                    cursorColor = primary,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                "Save to Folder",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }

                        HorizontalDivider(color = colorResource(id = R.color.divider_color))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(folders) { folder ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            folder.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = textPrimary
                                        )
                                    },
                                    colors = androidx.compose.material3.ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.saveNoteToFolder(folder.name)
                                        onNavigateToHome()
                                    }
                                )
                                HorizontalDivider(color = colorResource(id = R.color.divider_color))
                            }
                        }

                        Button(
                            onClick = onNavigateToNewFolder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = Color.White
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "New Folder",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                "Create New Folder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                is PollingUiState.Error -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
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
                            Column(modifier = Modifier.padding(32.dp)) {
                                Text(
                                    "Error",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    state.message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }

                is PollingUiState.Timeout -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
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
                            Column(modifier = Modifier.padding(32.dp)) {
                                Text(
                                    "Timeout",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Polling timed out. Please try again.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }

                is PollingUiState.Idle -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
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
                            Column(modifier = Modifier.padding(32.dp)) {
                                Text(
                                    "No transcription in progress",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}