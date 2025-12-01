package com.cs407.lazynotes.recording

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.cs407.lazynotes.data.repository.FirefliesRepository
import com.cs407.lazynotes.data.repository.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

private suspend fun copyUriToAppFile(context: Context, uri: Uri, explicitName: String? = null): File? {
    return withContext(Dispatchers.IO) {
        try {
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
            val uploadsDir = File(baseDir, "uploads").apply { if (!exists()) mkdirs() }

            val resolver: ContentResolver = context.contentResolver
            val name = explicitName ?: run {
                var suggested = "upload_${System.currentTimeMillis()}.m4a"
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        val disp = cursor.getString(nameIndex)
                        if (!disp.isNullOrBlank()) suggested = disp
                    }
                }
                suggested
            }

            val outFile = File(uploadsDir, name)
            resolver.openInputStream(uri).use { input: InputStream? ->
                if (input == null) return@withContext null
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            outFile
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun UploadingRoute(
    navController: NavController,
    onNavigateToHome: () -> Unit,
    onNavigateToFolderSelect: (String?) -> Unit,
    repository: FirefliesRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isProcessing by remember { mutableStateOf(false) }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null || isProcessing) return@rememberLauncherForActivityResult

        isProcessing = true
        scope.launch {
            val copied: File? = copyUriToAppFile(context, uri)
            if (copied == null) {
                isProcessing = false
                onNavigateToFolderSelect(null)
                return@launch
            }

            val noteTitle = copied.nameWithoutExtension

            when (val result = repository.processRecordingForTranscription(copied, noteTitle)) {
                is NetworkResult.Success -> {
                    val clientRefId = result.data
                    isProcessing = false
                    onNavigateToFolderSelect(clientRefId)
                }
                is NetworkResult.Failure -> {
                    println("ERROR: Failed to start transcription job. Message: ${result.message}")
                    isProcessing = false
                    onNavigateToFolderSelect(null)
                }
            }
        }
    }

    // No auto-launch here. Picker opens only when user taps the box/button on the screen.
    com.cs407.lazynotes.ui.screens.uploadFileScreen(
        navController = navController,
        onNavigateToHome = onNavigateToHome,
        onNavigateToUploadFileBrowse = {
            if (!isProcessing) pickAudioLauncher.launch("audio/*")
        }
    )
}