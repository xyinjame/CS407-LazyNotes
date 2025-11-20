package com.cs407.lazynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.lazynotes.recording.RecordingRoute
import com.cs407.lazynotes.ui.screens.*
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
    val context = LocalContext.current
    val loginViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(context.applicationContext as android.app.Application)
    )

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginPage(
                viewModel = loginViewModel,
                onNavigateToAskName = {
                    navController.navigate("askName") { popUpTo("login") { inclusive = true } }
                },
                onNavigateToHome = {
                    navController.navigate("home_main") { popUpTo("login") { inclusive = true } }
                }
            )
        }
        composable("askName") {
            AskNamePage(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    navController.navigate("home_main") { popUpTo("askName") { inclusive = true } }
                }
            )
        }
        composable("login_home") {
            HomePage(
                viewModel = loginViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") { popUpTo("login_home") { inclusive = true } }
                }
            )
        }

        composable("home_main") {
            HomeScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToNew = { navController.navigate("newFolderNotes") },
                onNavigateToViewNotes = { navController.navigate("viewNote") }
            )
        }
        composable("settings") {
            SettingsScreen(
                navController = navController,
                onNavigateToHome = { navController.navigate("home_main") },
                onNavigateToPreferences = { navController.navigate("preferences") }
            )
        }
        composable("newFolderNotes") {
            NewFolderNotesScreen(
                navController = navController,
                onNavigateToNewFolder = { navController.navigate("newFolder") },
                onNavigateToNewNote = { navController.navigate("newNote") }
            )
        }
        composable("viewNote") {
            NoteScreen(
                navController = navController,
                onNavigateToHome = { navController.navigate("home_main") }
            )
        }
        composable("newFolder") {
            NewFolderScreen(
                navController = navController,
                onNavigateToHome = { navController.navigate("home_main") }
            )
        }
        composable("newNote") {
            NewNoteScreen(
                navController = navController,
                onNavigateToHome = { navController.navigate("home_main") },
                onNavigateToRecord = { navController.navigate("record") },
                onNavigateToUpload = { navController.navigate("upload") }
            )
        }
        composable("preferences") {
            preferenceScreen(onNavigateToHome = { navController.navigate("home_main") })
        }
        composable("record") {
            RecordingRoute(
                navController = navController,
                onNavigateToHome = { navController.navigate("home_main") },
                onNavigateToFolderSelect = {
                    navController.navigate("folderSelect") {
                        popUpTo("record") { inclusive = true }
                    }
                }
            )
        }
        composable("upload") {
            uploadFileScreen(
                navController = navController,
                onNavigateToHome = { navController.navigate("home_main") },
                onNavigateToUploadFileBrowse = { navController.navigate("uploadFileBrowse") }
            )
        }
        composable("folderSelect") {
            FolderSelectScreen(
                navController = navController,
                onNavigateToHome = { navController.navigate("home_main") },
                onNavigateToNewFolder = { navController.navigate("newFolder") }
            )
        }
        composable("uploadFileBrowse") {
            uploadFileBrowse(
                navController = navController,
                onNavigateToHome = { navController.navigate("home_main") }
            )
        }
    }
}