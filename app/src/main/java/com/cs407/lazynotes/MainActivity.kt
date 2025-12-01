package com.cs407.lazynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs407.lazynotes.data.FolderRepository
import com.cs407.lazynotes.data.NoteRepository
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
import com.cs407.lazynotes.ui.theme.LazyNotesTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

// --- Route Constants --- We define these to avoid typos and for easy reference
private const val FOLDER_SELECT_ROUTE = "folderSelect"
private const val NOTE_LIST_ROUTE = "noteList"
private const val NOTE_DETAIL_ROUTE = "noteDetail"

private const val CLIENT_REF_ID_ARG = "clientRefId"
private const val AUDIO_URI_ARG = "audioUri"
private const val FOLDER_NAME_ARG = "folderName"
private const val NOTE_ID_ARG = "noteId"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
        }

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

    val folderSelectViewModel: FolderSelectViewModel = viewModel(
        factory = FolderSelectViewModel.provideFactory(
            firefliesRepo = firefliesRepository,
            folderRepo = FolderRepository,
            noteRepo = NoteRepository
        )
    )

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
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
                    navController.navigate("$NOTE_DETAIL_ROUTE/$noteId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("$NOTE_DETAIL_ROUTE/{$NOTE_ID_ARG}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString(NOTE_ID_ARG)
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // --- Creation and Selection Flow ---
        composable("newFolderNotes") { NewFolderNotesScreen(navController = navController, onNavigateToNewFolder = {navController.navigate("newFolder")}, onNavigateToNewNote = {navController.navigate("newNote")}) }
        // Updated composable for NewFolderScreen
        composable("newFolder") { NewFolderScreen(navController = navController) }
        composable("newNote") { NewNoteScreen(navController = navController, onNavigateToHome = {navController.navigate("home")}, onNavigateToRecord = {navController.navigate("record")}, onNavigateToUpload = {navController.navigate("upload")}) }

        composable("record") {
            RecordingRoute(
                navController = navController,
                onNavigateToHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                repository = firefliesRepository,
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
                    val route = "$FOLDER_SELECT_ROUTE?$CLIENT_REF_ID_ARG=$clientRefId&$AUDIO_URI_ARG=$audioUri"
                    navController.navigate(route) { popUpTo("uploadFileBrowse") { inclusive = true } }
                }
            )
        }

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
                viewModel = folderSelectViewModel
            )
        }
        
        // --- Settings Flow ---
        composable("settings") { SettingsScreen(navController = navController, onNavigateToHome = {navController.navigate("home")}, onNavigateToPreferences = {navController.navigate("preferences")}) }
        composable("preferences") { preferenceScreen(onNavigateToHome = {navController.navigate("home")}) }
    }
}
