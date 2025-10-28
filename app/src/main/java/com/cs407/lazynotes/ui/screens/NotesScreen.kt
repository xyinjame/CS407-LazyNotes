package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.lazynotes.R


@Composable
fun NotesScreen() {

    // Variables used to change the text of each button and transcript
    var sliderPosition by remember {mutableStateOf(0f)}
    var recordingButtonText by remember {mutableStateOf(R.string.play_recording)}
    var transcriptLayout by remember {mutableStateOf(R.string.transcript)}
    var transcript by remember {mutableStateOf("Bullet Points")}

    // Check what the transcript layout the user wants and change transcript accordingly
    if (transcriptLayout == R.string.transcript) {
        transcript = "Bullet Points"
    } else {
        transcript = "Transcript"
    }

    // Main container that houses all elements
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Top row that contains the note name and button to close the current note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(10.dp, 25.dp, 10.dp, 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                // Current Note Name
                Text(
                    text = "Placeholder Note Name",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                )

                // Button to exit note
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Page",
                        tint = Color.Black
                    )
                }

            }
        }

        // Play/pause recording button
        OutlinedButton(
            onClick = {
                if (recordingButtonText == R.string.play_recording) {
                    recordingButtonText = R.string.pause_recording
                } else {
                    recordingButtonText = R.string.play_recording
                }
            },
            colors = ButtonDefaults.buttonColors(
                   containerColor = Color.DarkGray,
            ),
            shape = RectangleShape,
            border = BorderStroke(2.dp, Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(
                text = stringResource(id = recordingButtonText)
            )
        }

        // Transcript/bullet point button
        OutlinedButton(
            onClick = {
                if (transcriptLayout == R.string.transcript) {
                    transcriptLayout = R.string.bullet_points
                } else {
                    transcriptLayout = R.string.transcript
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray,
            ),
            shape = RectangleShape,
            border = BorderStroke(2.dp, Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(
                text = stringResource(id = transcriptLayout)
            )
        }

        // Audio file slider
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                sliderPosition = newValue
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.DarkGray,
                inactiveTrackColor = Color.Gray
            ),
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 20.dp)
                .height(30.dp)
        )

        // Transcript of the audio file
        Text(
            text = transcript,
            modifier = Modifier
                .padding(10.dp, 0.dp, 10.dp, 10.dp)
        )

    }
}

