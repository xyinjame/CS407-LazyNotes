package com.cs407.lazynotes.data.storage

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.File
import kotlinx.coroutines.tasks.await // Turn Task to suspend function

class FirebaseStorageServiceImpl : StorageService {

    // Get Firebase Storage reference
    private val storageRef = Firebase.storage.reference

    override suspend fun uploadAudioFile(localFile: File): String? {
        // 1. Get local file Uri
        val audioFileUri = Uri.fromFile(localFile)
        val fileName = localFile.name

        // 2. Create Storage Reference，like: "audio_recordings/recording_1700000000000.m4a"
        val audioRef = storageRef.child("audio_recordings/$fileName")

        return try {
            // 3. Execute putFile Uploading mission，use .await() until completed
            audioRef.putFile(audioFileUri).await()

            // 4. After uploading，get public downloading URL
            val downloadUrl = audioRef.downloadUrl.await()

            // 5. return HTTPS string fireflies needs
            downloadUrl.toString()

        } catch (e: Exception) {
            // if upload or get url fails
            println("Firebase Storage Upload Failed: ${e.message}")
            localFile.delete() // upload fails, clean local files
            null
        }
    }
}