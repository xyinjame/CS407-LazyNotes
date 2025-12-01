package com.cs407.lazynotes.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.cs407.lazynotes.data.repository.FirefliesRepository
import com.cs407.lazynotes.data.repository.NetworkResult
import com.cs407.lazynotes.util.FileUtil
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun uploadFileBrowse(
    navController: NavController,
    onNavigateToHome: () -> Unit,
    repository: FirefliesRepository,
    onNavigateToFolderSelect: (clientRefId: String?, audioUri: String?) -> Unit // Updated signature
) {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedFileUri = uri
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Upload a File") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { filePickerLauncher.launch("audio/*") }) {
                Text("Select Audio File")
            }

            if (isLoading) {
                CircularProgressIndicator()
            }

            error?.let {
                Text(text = it)
            }
        }
    }

    LaunchedEffect(selectedFileUri) {
        selectedFileUri?.let {
            isLoading = true
            coroutineScope.launch {
                val localFile: File? = try {
                    FileUtil.from(context, it)
                } catch (e: Exception) {
                    error = "Failed to create a local file copy."
                    null
                }

                if (localFile != null) {
                    when (val result = repository.processRecordingForTranscription(localFile, localFile.nameWithoutExtension)) {
                        is NetworkResult.Success -> {
                            onNavigateToFolderSelect(result.data, it.toString()) // Pass both clientRefId and audioUri
                        }
                        is NetworkResult.Failure -> {
                            error = result.message
                            isLoading = false
                        }
                    }
                } else {
                    isLoading = false
                }
            }
        }
    }
}
