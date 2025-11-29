package com.cs407.lazynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.lazynotes.data.network.RetrofitClient
import com.cs407.lazynotes.data.repository.FirefliesRepository
import com.cs407.lazynotes.data.storage.FirebaseStorageServiceImpl
import com.cs407.lazynotes.recording.RecordingRoute
import com.cs407.lazynotes.ui.screens.FolderSelectScreen
import com.cs407.lazynotes.ui.screens.HomeScreen
import com.cs407.lazynotes.ui.screens.NewFolderNotesScreen
import com.cs407.lazynotes.ui.screens.NewFolderScreen
import com.cs407.lazynotes.ui.screens.NewNoteScreen
import com.cs407.lazynotes.ui.screens.NoteScreen
import com.cs407.lazynotes.ui.screens.SettingsScreen
import com.cs407.lazynotes.ui.screens.preferenceScreen
import com.cs407.lazynotes.ui.screens.uploadFileBrowse
import com.cs407.lazynotes.ui.screens.uploadFileScreen
import com.cs407.lazynotes.ui.theme.LazyNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LazyNotesTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val firefliesService = RetrofitClient.firefliesService
    val storageService = FirebaseStorageServiceImpl()
    val firefliesRepository = FirefliesRepository(firefliesService, storageService)

    NavHost (
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToSettings = {navController.navigate("settings")},
                onNavigateToNew = {navController.navigate("newFolderNotes")},
                onNavigateToViewNotes = {navController.navigate("viewNote")}
            )
        }

        composable("settings") {
            SettingsScreen(
                navController = navController,
                onNavigateToHome = {navController.navigate("home")},
                onNavigateToPreferences = {navController.navigate("preferences")}
            )
        }

        composable("newFolderNotes") {
            NewFolderNotesScreen(
                navController = navController,
                onNavigateToNewFolder = {navController.navigate("newFolder")},
                onNavigateToNewNote = {navController.navigate("newNote")}
            )
        }

        composable("viewNote") {
            NoteScreen(
                navController = navController,
                onNavigateToHome = {navController.navigate("home")}
            )
        }

        composable("newFolder") {
            NewFolderScreen(
                navController = navController,
                onNavigateToHome = {navController.navigate("home")}
            )
        }

        composable("newNote") {
            NewNoteScreen(
                navController = navController,
                onNavigateToHome = {navController.navigate("home")},
                onNavigateToRecord = {navController.navigate("record")},
                onNavigateToUpload = {navController.navigate("upload")}
            )
        }

        composable("preferences") {
            preferenceScreen(onNavigateToHome = {navController.navigate("home")})
        }

        // After finishing a recording, remove "record" from the back stack
        // so you can't go back into a finished recording session.
        composable("record") {
            RecordingRoute(
                navController = navController,
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToFolderSelect = {
                    navController.navigate("folderSelect") {
                        popUpTo("record") { inclusive = true }
                    }
                },
                repository = firefliesRepository
            )
        }

        composable("upload") {
            uploadFileScreen(
                navController = navController,
                onNavigateToHome = {navController.navigate("home")},
                onNavigateToUploadFileBrowse = {navController.navigate("uploadFileBrowse")}
            )
        }

        composable("folderSelect") {
            FolderSelectScreen(
                navController = navController,
                onNavigateToHome = {navController.navigate("home")},
                onNavigateToNewFolder = {navController.navigate("newFolder")}
            )
        }

        composable("uploadFileBrowse") {
            uploadFileBrowse(
                navController = navController,
                onNavigateToHome = {navController.navigate("home")}
            )
        }

    }
}
