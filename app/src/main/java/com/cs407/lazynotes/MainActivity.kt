package com.cs407.lazynotes

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.lazynotes.data.UserViewModel
import com.cs407.lazynotes.recording.RecordingRoute
import com.cs407.lazynotes.ui.screens.*
import com.cs407.lazynotes.ui.theme.LazyNotesTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        enableEdgeToEdge()
        setContent {
            LazyNotesTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: UserViewModel = viewModel(),
                  navController: NavHostController = rememberNavController()) {

    val userState by viewModel.userState.collectAsState()

    LaunchedEffect(userState) {
        if (userState.id == 0 || userState.name.isEmpty()) {
            navController.navigate("login") {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
        } else {
            navController.navigate("home_main") {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginPage(Modifier) { viewModel.setUser(it) }
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
                onNavigateToPreferences = { navController.navigate("preferences") },
                navOut = { Firebase.auth.signOut() }
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