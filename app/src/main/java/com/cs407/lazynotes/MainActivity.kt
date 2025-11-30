package com.cs407.lazynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.cs407.lazynotes.ui.screens.FlashcardScreen
import com.cs407.lazynotes.ui.theme.LazyNotesTheme

// Route constants
private const val FOLDER_SELECT_ROUTE = "folderSelect"
private const val CLIENT_REF_ID_ARG = "clientRefId"

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
        // ... (other composables remain the same)
        composable("home") { HomeScreen(onNavigateToSettings = {navController.navigate("settings")}, onNavigateToNew = {navController.navigate("newFolderNotes")}, onNavigateToViewNotes = {navController.navigate("viewNote")}) }
        composable("settings") { SettingsScreen(navController = navController, onNavigateToHome = {navController.navigate("home")}, onNavigateToPreferences = {navController.navigate("preferences")}) }
        composable("newFolderNotes") { NewFolderNotesScreen(navController = navController, onNavigateToNewFolder = {navController.navigate("newFolder")}, onNavigateToNewNote = {navController.navigate("newNote")}) }
        composable("viewNote") { NoteScreen(navController = navController, onNavigateToHome = {navController.navigate("home")}) }
        composable("newFolder") { NewFolderScreen(navController = navController, onNavigateToHome = {navController.navigate("home")}) }
        composable("newNote") { NewNoteScreen(navController = navController, onNavigateToHome = {navController.navigate("home")}, onNavigateToRecord = {navController.navigate("record")}, onNavigateToUpload = {navController.navigate("upload")}) }
        composable("preferences") { preferenceScreen(onNavigateToHome = {navController.navigate("home")}) }
        composable("upload") { uploadFileScreen(navController = navController, onNavigateToHome = {navController.navigate("home")}, onNavigateToUploadFileBrowse = {navController.navigate("uploadFileBrowse")}) }
        composable("uploadFileBrowse") { uploadFileBrowse(navController = navController, onNavigateToHome = {navController.navigate("home")}) }
        composable("home") { HomeScreen(onNavigateToSettings = {navController.navigate("settings")}, onNavigateToNew = {navController.navigate("newFolderNotes")}, onNavigateToViewNotes = {navController.navigate("viewNote")}) }

        composable("record") {
            RecordingRoute(
                navController = navController,
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToFolderSelect = { clientRefId ->
                    // This navigation logic is correct.
                    // It constructs a route like "folderSelect?clientRefId=someId"
                    val route = "$FOLDER_SELECT_ROUTE?$CLIENT_REF_ID_ARG=$clientRefId"
                    navController.navigate(route) {
                        popUpTo("record") { inclusive = true }
                    }
                },
                repository = firefliesRepository
            )
        }

        composable("flashcards") {
            val fakeTranscript = """
        Today we discussed Dijkstra's algorithm, shortest path in graphs,
        and how priority queues are used to always pick the next closest node.
        We also compared it to Bellman-Ford and talked about negative edges.
    """.trimIndent()

            FlashcardScreen(transcript = fakeTranscript)
        }

        composable(
            // Define the route with the optional parameter syntax
            route = "folderSelect?clientRefId={clientRefId}",
            // Define the argument and its type
            arguments = listOf(navArgument("clientRefId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            // Retrieve the argument from the backStackEntry
            val clientRefId = backStackEntry.arguments?.getString("clientRefId")
            FolderSelectScreen(
                navController = navController,
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToNewFolder = { navController.navigate("newFolder") },
                clientRefId = clientRefId // Pass it to your screen
            )
        }
    }
}
