package com.cs407.lazynotes.recording

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.cs407.lazynotes.recording.RecordingController
import com.cs407.lazynotes.ui.screens.RecordingScreen
import androidx.navigation.NavController

/*
Recordings are saved here: Click on Device Explorer -> /sdcard/Android/data/com.cs407.lazynotes/files
 */
@Composable
fun RecordingRoute(
    navController: NavController,
    onNavigateToHome: () -> Unit,
    onNavigateToFolderSelect: () -> Unit
) {
    val context = LocalContext.current
    val controller = remember { RecordingController(context) }

    var hasMic by remember { mutableStateOf(false) }
    val requestMic = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasMic = granted }

    LaunchedEffect(Unit) {
        requestMic.launch(Manifest.permission.RECORD_AUDIO)
    }

    RecordingScreen(
        navController = navController,
        onNavigateToHome = {
            controller.discard()
            onNavigateToHome()
        },
        onNavigateToFolderSelect = {
            controller.stop()
            onNavigateToFolderSelect()
        },
        onStartRecording = {
            if (hasMic) controller.start() else requestMic.launch(Manifest.permission.RECORD_AUDIO)
        },
        onPause = { controller.pause() },
        onResume = { controller.resume() }
    )
}