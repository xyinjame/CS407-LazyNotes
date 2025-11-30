package com.cs407.lazynotes.data.storage

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseStorageServiceImpl : StorageService {

    private val storage = FirebaseStorage.getInstance()

    override suspend fun uploadAudioFile(localFile: File): String? {
        return try {
            val storageRef = storage.reference
            val fileRef = storageRef.child("audio/${localFile.name}")
            fileRef.putFile(android.net.Uri.fromFile(localFile)).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
