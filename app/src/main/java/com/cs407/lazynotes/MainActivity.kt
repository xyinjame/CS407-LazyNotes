package com.cs407.lazynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.lazynotes.ui.screens.HomeScreen
import com.cs407.lazynotes.ui.screens.NewFolderNotesScreen
import com.cs407.lazynotes.ui.screens.NewFolderScreen
import com.cs407.lazynotes.ui.screens.NewNoteScreen
import com.cs407.lazynotes.ui.screens.NoteScreen
import com.cs407.lazynotes.ui.screens.SettingsScreen
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
                navController = navController
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
            NoteScreen(navController = navController)
        }

        composable("newFolder") {
            NewFolderScreen()
        }

        composable("newNote") {
            NewNoteScreen()
        }

    }
}