package com.cs407.lazynotes.ui.screens

import android.R.attr.title
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.lazynotes.data.Preferences
import com.cs407.lazynotes.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.draw.shadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun preferenceScreen(
    onNavigateToHome: () -> Unit
) {

    val primary = colorResource(id = R.color.primary_blue)
    val secondary = colorResource(id = R.color.secondary_teal)
    val background = colorResource(id = R.color.background_light)
    val surface = colorResource(id = R.color.surface_white)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)
    val dividerColor = colorResource(id = R.color.divider_color)

    Scaffold (
        containerColor = background,
        topBar = {
            TopAppBar (
                title = {
                    Text(
                        text = "Preferences",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = textPrimary,
                    actionIconContentColor = primary
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

            PreferenceCard {
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text (
                        text = "Default Text Layout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Observe global preference
                    val showTranscriptFirst by Preferences.showTranscriptFirst.collectAsState(initial = true)

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Transcript",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textPrimary
                            )
                            RadioButton(
                                selected = showTranscriptFirst,
                                onClick = { Preferences.setShowTranscriptFirst(true) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = secondary,
                                    unselectedColor = textSecondary
                                )
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Summary",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textPrimary
                            )
                            RadioButton(
                                selected = !showTranscriptFirst,
                                onClick = { Preferences.setShowTranscriptFirst(false) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = secondary,
                                    unselectedColor = textSecondary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Default Folder State",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Observe global preference
                    val openByDefault by Preferences.folderDefaultOpen.collectAsState(initial = false)

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Closed",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textPrimary
                            )
                            RadioButton(
                                selected = !openByDefault,
                                onClick = { Preferences.setFolderDefaultOpen(false) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = secondary,
                                    unselectedColor = textSecondary
                                )
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Open",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textPrimary
                            )
                            RadioButton(
                                selected = openByDefault,
                                onClick = { Preferences.setFolderDefaultOpen(true) },
                                colors = RadioButtonDefaults.colors (
                                    selectedColor = secondary,
                                    unselectedColor = textSecondary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PreferenceCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Default Folder Layout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Observe global preference
                    val alphabetical by Preferences.folderSortAlphabetical.collectAsState(initial = false)

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Recently Edited",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textPrimary
                            )
                            RadioButton(
                                selected = !alphabetical,
                                onClick = { Preferences.setFolderSortAlphabetical(false) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = secondary,
                                    unselectedColor = textSecondary
                                )
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Alphabetical",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textPrimary
                            )
                            RadioButton(
                                selected = alphabetical,
                                onClick = { Preferences.setFolderSortAlphabetical(true) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = secondary,
                                    unselectedColor = textSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreferenceCard(
    content: @Composable () -> Unit
) {
    val surface = colorResource(id = R.color.surface_white)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = surface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        content()
    }
}