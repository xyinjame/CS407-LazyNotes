package com.cs407.lazynotes.data.repository

import com.cs407.lazynotes.BuildConfig
import com.cs407.lazynotes.data.network.FirefliesService
import com.cs407.lazynotes.data.network.models.GraphQLRequest
import com.cs407.lazynotes.data.storage.StorageService
import java.io.File

class FirefliesRepository(
    private val apiService: FirefliesService,
    private val storageService: StorageService
) {

    /**
     * 1. Upload audio to Firebase Storage。
     * 2. Use Fireflies.ai 'uploadAudio' mutation to start transcription
     * @param localFile local audio files。
     * @param title Title of Notes。
     * @return use for tracking: Client reference ID，return null if fails。
     */
    suspend fun processRecordingForTranscription(localFile: File, title: String): String? {

        // 1. Upload to Firebase
        val publicAudioUrl = storageService.uploadAudioFile(localFile)

        if (publicAudioUrl.isNullOrEmpty()) {
            println("Storage Upload failed. Aborting transcription.")
            return null
        }

        // 2. Fireflies API
        val authHeader = "Bearer ${BuildConfig.FIREFLIES_API_KEY}"
        val clientRefId = localFile.name // use file name as reference ID

        // Use 'uploadAudio' mutation
        val mutation = """
            mutation uploadAudio(${'$'}input: AudioUploadInput) { 
                uploadAudio(input: ${'$'}input) {
                    success
                    title
                    message
                }
            }
        """.trimIndent()

        val variables = mapOf(
            "input" to mapOf(
                "url" to publicAudioUrl,
                "title" to title,
                "client_reference_id" to clientRefId,
                // TODO: from preferenceScreen to read and add custom_language
            )
        )
        val request = GraphQLRequest(query = mutation, variables = variables)

        return try {
            val response = apiService.executeGraphQL(authHeader, request)

            if (response.errors.isNullOrEmpty()) {
                // upload successfully return ID
                clientRefId
            } else {
                // ... (error)
                null
            }
        } catch (e: Exception) {
            // ... (internet error)
            null
        }
    }
}