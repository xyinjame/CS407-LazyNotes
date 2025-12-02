package com.cs407.lazynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs407.lazynotes.data.FolderRepository
import com.cs407.lazynotes.data.NoteRepository
import com.cs407.lazynotes.data.UserViewModel
import com.cs407.lazynotes.data.network.RetrofitClient
import com.cs407.lazynotes.data.repository.FirefliesRepository
import com.cs407.lazynotes.data.storage.FirebaseStorageServiceImpl
import com.cs407.lazynotes.recording.RecordingRoute
import com.cs407.lazynotes.ui.screens.FolderSelectScreen
import com.cs407.lazynotes.ui.screens.FolderSelectViewModel
import com.cs407.lazynotes.ui.screens.HomeScreen
import com.cs407.lazynotes.ui.screens.NewFolderNotesScreen
import com.cs407.lazynotes.ui.screens.NewFolderScreen
import com.cs407.lazynotes.ui.screens.NewNoteScreen
import com.cs407.lazynotes.ui.screens.NoteDetailScreen
import com.cs407.lazynotes.ui.screens.NoteListScreen
import com.cs407.lazynotes.ui.screens.SettingsScreen
import com.cs407.lazynotes.ui.screens.preferenceScreen
import com.cs407.lazynotes.ui.screens.uploadFileBrowse
import com.cs407.lazynotes.ui.screens.uploadFileScreen
import com.cs407.lazynotes.ui.screens.FlashcardScreen
import com.cs407.lazynotes.ui.theme.LazyNotesTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cs407.lazynotes.ui.screens.LoginPage
import com.cs407.lazynotes.ui.theme.MainBackground

// --- Route Constants --- We define these to avoid typos and for easy reference
private const val FOLDER_SELECT_ROUTE = "folderSelect"
private const val NOTE_LIST_ROUTE = "noteList"
private const val NOTE_DETAIL_ROUTE = "noteDetail"

// --- Argument Keys --- Used for passing data between screens
private const val CLIENT_REF_ID_ARG = "clientRefId"
private const val AUDIO_URI_ARG = "audioUri"
private const val FOLDER_NAME_ARG = "folderName"
private const val NOTE_ID_ARG = "noteId"

/**
 * The main entry point of the application. This activity hosts the Jetpack Compose content
 * and sets up the navigation graph.
 */
class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        enableEdgeToEdge()

        // CRITICAL: Initialize Firebase services on app startup.
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        // CRITICAL: Install AppCheck providers. Use Debug for debug builds, and PlayIntegrity for release builds.
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
        }

        setContent {
            LazyNotesTheme {
                // Set up the application's navigation structure.
                AppNavigation()
            }
        }
    }
}

/**
 * This composable function defines the entire navigation graph of the app using NavHost.
 * It is responsible for creating all repositories, ViewModels, and defining routes.
 */
@Composable
fun AppNavigation(viewModel: UserViewModel = viewModel(),
                  navController: NavHostController = rememberNavController()) {

    val userState by viewModel.userState.collectAsState()

    // CRITICAL: Instantiate all singleton repositories and services here to be passed down.
    val firefliesService = RetrofitClient.firefliesService
    val storageService = FirebaseStorageServiceImpl()
    val firefliesRepository = FirefliesRepository(firefliesService, storageService)

    // CRITICAL: The FolderSelectViewModel is shared across multiple screens involved in the note creation flow.
    val folderSelectViewModel: FolderSelectViewModel = viewModel(
        factory = FolderSelectViewModel.provideFactory(
            firefliesRepo = firefliesRepository,
            folderRepo = FolderRepository,
            noteRepo = NoteRepository
        )
    )

    // NavHost is the container for all navigation destinations.
    val isLoggedIn = userState.id != 0 && userState.name.isNotEmpty()
    val startDestination = if (isLoggedIn) "home" else "login"
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginPage(Modifier) { viewModel.setUser(it) }
        }
        // --- Main Screens ---
        composable("home") {
            HomeScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToNew = { navController.navigate("newFolderNotes") },
                onNavigateToViewNotes = { folderName ->
                    navController.navigate("$NOTE_LIST_ROUTE/$folderName")
                }
            )
        }

        composable("$NOTE_LIST_ROUTE/{$FOLDER_NAME_ARG}") { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString(FOLDER_NAME_ARG)
            NoteListScreen(
                folderName = folderName,
                onNoteClick = { noteId ->
                    // CRITICAL: Navigate to note detail screen with the unique note ID.
                    navController.navigate("$NOTE_DETAIL_ROUTE/$noteId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("$NOTE_DETAIL_ROUTE/{$NOTE_ID_ARG}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString(NOTE_ID_ARG)

            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() },
                onGenerateFlashcards = { transcript ->
                    if (noteId != null) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("flashcardNoteId", noteId)

                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("flashcardTranscript", transcript)

                        navController.navigate("flashcards")
                    }
                }
            )
        }

        // --- Creation and Selection Flow ---
        composable("newFolderNotes") { NewFolderNotesScreen(navController = navController, onNavigateToNewFolder = {navController.navigate("newFolder")}, onNavigateToNewNote = {navController.navigate("newNote")}) }
        composable("newFolder") { NewFolderScreen(navController = navController) }
        composable("newNote") { NewNoteScreen(navController = navController, onNavigateToHome = {navController.navigate("home")}, onNavigateToRecord = {navController.navigate("record")}, onNavigateToUpload = {navController.navigate("upload")}) }

        composable("record") {
            RecordingRoute(
                navController = navController,
                onNavigateToHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                repository = firefliesRepository,
                // CRITICAL: After transcription is initiated, navigate to folder select screen with the necessary IDs.
                onNavigateToFolderSelect = { clientRefId, audioUri ->
                    val route = "$FOLDER_SELECT_ROUTE?$CLIENT_REF_ID_ARG=$clientRefId&$AUDIO_URI_ARG=$audioUri"
                    navController.navigate(route) { popUpTo("record") { inclusive = true } }
                }
            )
        }

        composable("upload") { uploadFileScreen(navController = navController, onNavigateToHome = {navController.navigate("home")}, onNavigateToUploadFileBrowse = {navController.navigate("uploadFileBrowse")}) }

        composable("uploadFileBrowse") {
            uploadFileBrowse(
                navController = navController,
                onNavigateToHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                repository = firefliesRepository,
                onNavigateToFolderSelect = { clientRefId, audioUri ->
                    // CRITICAL: Same as recording flow, navigate to folder select with transcription and audio data.
                    val route = "$FOLDER_SELECT_ROUTE?$CLIENT_REF_ID_ARG=$clientRefId&$AUDIO_URI_ARG=$audioUri"
                    navController.navigate(route) { popUpTo("uploadFileBrowse") { inclusive = true } }
                }
            )
        }

        composable("flashcards") {
            val prevEntry = navController.previousBackStackEntry

            val noteId = prevEntry
                ?.savedStateHandle
                ?.get<String>("flashcardNoteId")
                ?: ""

            val transcript = prevEntry
                ?.savedStateHandle
                ?.get<String>("flashcardTranscript")
                ?: ""

            FlashcardScreen(
                noteId = noteId,
                transcript = transcript,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // CRITICAL: This destination handles the final step of note creation, receiving transcription data.
        composable(
            route = "$FOLDER_SELECT_ROUTE?$CLIENT_REF_ID_ARG={$CLIENT_REF_ID_ARG}&$AUDIO_URI_ARG={$AUDIO_URI_ARG}",
            arguments = listOf(
                navArgument(CLIENT_REF_ID_ARG) { type = NavType.StringType; nullable = true },
                navArgument(AUDIO_URI_ARG) { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val clientRefId = backStackEntry.arguments?.getString(CLIENT_REF_ID_ARG)
            val audioUri = backStackEntry.arguments?.getString(AUDIO_URI_ARG)
            FolderSelectScreen(
                navController = navController,
                onNavigateToHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToNewFolder = { navController.navigate("newFolder") },
                clientRefId = clientRefId,
                audioUri = audioUri, // Pass the audio URI
                viewModel = folderSelectViewModel // Use the shared ViewModel
            )
        }

        // --- Settings Flow ---
        composable("settings") { SettingsScreen(navController = navController, onNavigateToHome = {navController.navigate("home")}, onNavigateToPreferences = {navController.navigate("preferences")}, navOut = { Firebase.auth.signOut() }) }
        composable("preferences") { preferenceScreen(onNavigateToHome = {navController.navigate("home")}) }
    }
}

@Composable
private fun Splash() {
    // Replace with your own branding. Keep it simple to avoid layout jank.
    androidx.compose.material3.Surface(modifier = Modifier) {}
}