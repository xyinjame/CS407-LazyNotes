package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.lazynotes.data.Preferences

@Composable
fun preferenceScreen(
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(10.dp, 25.dp, 10.dp, 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Preference",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 15.dp)
                )
                Button(
                    onClick = { onNavigateToHome() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Home",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            color = Color.Black,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Column(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
            Text(text = "Default Text Layout", fontSize = 16.sp, color = Color.Black)

            // Observe global preference
            val showTranscriptFirst by Preferences.showTranscriptFirst.collectAsState(initial = true)

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Transcript", fontSize = 14.sp, color = Color.Black)
                    RadioButton(
                        selected = showTranscriptFirst,
                        onClick = { Preferences.setShowTranscriptFirst(true) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF9C27B0),
                            unselectedColor = Color.Gray
                        )
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 20.dp)
                ) {
                    Text(text = "Summary", fontSize = 14.sp, color = Color.Black)
                    RadioButton(
                        selected = !showTranscriptFirst,
                        onClick = { Preferences.setShowTranscriptFirst(false) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF9C27B0),
                            unselectedColor = Color.Gray
                        )
                    )
                }
            }
        }
        HorizontalDivider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Column(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
            Text(text = "Default Folder State", fontSize = 16.sp, color = Color.Black)

            // Observe global preference
            val openByDefault by Preferences.folderDefaultOpen.collectAsState(initial = false)

            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Closed", fontSize = 14.sp, color = Color.Black)
                    RadioButton(
                        selected = !openByDefault,
                        onClick = { Preferences.setFolderDefaultOpen(false) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF9C27B0),
                            unselectedColor = Color.Gray
                        )
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 20.dp)
                ) {
                    Text(text = "Open", fontSize = 14.sp, color = Color.Black)
                    RadioButton(
                        selected = openByDefault,
                        onClick = { Preferences.setFolderDefaultOpen(true) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF9C27B0),
                            unselectedColor = Color.Gray
                        )
                    )
                }
            }
        }
        HorizontalDivider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Column(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
            Text(text = "Default Folder Layout", fontSize = 16.sp, color = Color.Black)

            // Observe global preference
            val alphabetical by Preferences.folderSortAlphabetical.collectAsState(initial = false)

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Recently Edited", fontSize = 14.sp, color = Color.Black)
                    RadioButton(
                        selected = !alphabetical,
                        onClick = { Preferences.setFolderSortAlphabetical(false) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF9C27B0),
                            unselectedColor = Color.Gray
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 20.dp)) {
                    Text(text = "Alphabetical", fontSize = 14.sp, color = Color.Black)
                    RadioButton(
                        selected = alphabetical,
                        onClick = { Preferences.setFolderSortAlphabetical(true) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF9C27B0),
                            unselectedColor = Color.Gray
                        )
                    )
                }
            }
        }
    }
}