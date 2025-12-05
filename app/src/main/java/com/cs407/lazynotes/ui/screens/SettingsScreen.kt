package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs407.lazynotes.R
import com.cs407.lazynotes.ui.theme.MainBackground
import com.cs407.lazynotes.ui.theme.TopBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.cs407.lazynotes.data.FolderRepository
import com.cs407.lazynotes.data.NoteRepository
// Added for sign-out fix - start
import com.cs407.lazynotes.ui.screens.getYourFirebaseAuth
import com.cs407.lazynotes.ui.screens.signOutCustomAuth
import kotlinx.coroutines.launch

// Added for sign-out fix - end


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onNavigateToHome: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    navOut: () -> Unit,
) {

    val primary = colorResource(id = R.color.primary_blue)
    val accent = colorResource(id = R.color.accent_coral)
    val background = colorResource(id = R.color.background_light)
    val surface = colorResource(id = R.color.surface_white)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)

    // Added for sign-out fix - start
    val yourAuth = getYourFirebaseAuth()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Added for sign-out fix - end

    var showDeleteDialog by remember{ mutableStateOf(false) }

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = textPrimary,
                    navigationIconContentColor = primary
                ),
                modifier = Modifier.shadow(elevation = 2.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(background)
                .padding(20.dp)
        ) {
            // Preference Settings Option
            SelectionCard(
                title = stringResource(id = R.string.preference),
                onClick = { onNavigateToPreferences() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SelectionCard(
                title = "Sign out",
                onClick = {
                    // Added for sign-out fix - start
                    signOutCustomAuth(yourAuth)
                    FolderRepository.clear()
                    NoteRepository.clear()
                    // Added for sign-out fix - end
                    navOut()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {showDeleteDialog = true}
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = accent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Delete Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {showDeleteDialog = false},
                title = {
                    Text(
                        "Delete Account?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to delete your account? This action cannot be undone and all your notes will be permanently deleted.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textPrimary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            val currentUser = yourAuth.currentUser
                            if(currentUser != null) {
                                scope.launch {
                                    deleteLocalUserData(context,currentUser.uid)
                                    FolderRepository.clear()
                                    NoteRepository.clear()
                                    deleteUserAccount(yourAuth) {success, exception ->
                                        if (success) {
                                            navOut()
                                        } else {
                                            println("Failed to delete account: ${exception?.message}")
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "Delete",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(
                            "Cancel",
                            color = textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                containerColor = surface
            )
        }

    }
}
