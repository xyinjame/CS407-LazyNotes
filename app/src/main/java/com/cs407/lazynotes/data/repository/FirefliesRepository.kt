package com.cs407.lazynotes.data.repository

import com.cs407.lazynotes.BuildConfig
import com.cs407.lazynotes.data.network.FirefliesService
import com.cs407.lazynotes.data.network.models.GraphQLResponse
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.cs407.lazynotes.data.storage.StorageService
import java.io.File

/**
 * A sealed class to represent the result of a network operation, which can either be a Success or a Failure.
 */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Failure(val error: Throwable?, val message: String) : NetworkResult<Nothing>()
}

// --- Data Models for JSON parsing ---

data class TranscriptSummary(
    val overview: String?,
    @SerializedName("action_items")
    val actionItems: String?,
    val keywords: List<String>?,
    val outline: List<String>?
)

private data class UploadAudioResponse(val uploadAudio: UploadAudioPayload?)
private data class UploadAudioPayload(val success: Boolean, val message: String?)

private data class GetTranscriptsResponseData(val transcripts: List<Transcript>?)

// CRITICAL: This is the main data model for a transcript from the Fireflies API.
data class Transcript(val id: String?, val title: String?, val summary: TranscriptSummary?, val sentences: List<Sentence>?)
data class Sentence(val raw_text: String?)

/**
 * Repository responsible for handling all interactions with the Fireflies.ai API.
 * It abstracts the data source (network and storage) from the rest of the app.
 */
class FirefliesRepository(
    private val apiService: FirefliesService,
    private val storageService: StorageService // For uploading audio files
) {
    private val gson = Gson() // For parsing JSON responses

    /**
     * Processes a recording by first uploading it to cloud storage, then sending the URL to Fireflies for transcription.
     * @param localFile The local audio file to be uploaded.
     * @param title The title to be assigned to the transcript.
     * @return A NetworkResult containing the client reference ID on success, or an error on failure.
     */
    @Suppress("GraphQLUnresolvedReference")
    suspend fun processRecordingForTranscription(localFile: File, title: String): NetworkResult<String> {
        return try {
            // CRITICAL: The audio file must first be uploaded to a public URL for Fireflies to access it.
            val publicAudioUrl = storageService.uploadAudioFile(localFile)
            if (publicAudioUrl.isNullOrEmpty()) {
                return NetworkResult.Failure(null, "Firebase Storage Upload failed, URL is empty.")
            }
            println("Firebase Public URL: $publicAudioUrl")

            val authHeader = "Bearer ${BuildConfig.FIREFLIES_API_KEY}"
            val clientRefId = localFile.name // Use the local file name as a unique identifier

            // Define the GraphQL mutation for uploading audio.
            val mutation = """
                mutation uploadAudio(${'$'}input: AudioUploadInput!) { 
                    uploadAudio(input: ${'$'}input) { success message }
                }
            """.trimIndent()

            val mutationInput = mapOf(
                "url" to publicAudioUrl,
                "title" to title,
                "client_reference_id" to clientRefId
            )
            val requestBody = mapOf("query" to mutation, "variables" to mapOf("input" to mutationInput))
            
            // CRITICAL: Execute the GraphQL request.
            val response = apiService.executeGraphQL(authHeader, requestBody)

            if (response.data != null && response.errors.isNullOrEmpty()) {
                val uploadResponse = gson.fromJson(response.data.toString(), UploadAudioResponse::class.java)
                if (uploadResponse.uploadAudio?.success == true) {
                    NetworkResult.Success(clientRefId) // Return the ID for polling
                } else {
                    val failureMessage = uploadResponse.uploadAudio?.message ?: "API indicated failure."
                    NetworkResult.Failure(null, failureMessage)
                }
            } else {
                val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown API error."
                NetworkResult.Failure(null, errorMessage)
            }
        } catch (e: Exception) {
            NetworkResult.Failure(e, "An unexpected error occurred: ${e.message}")
        }
    }

    /**
     * Fetches the list of transcripts from Fireflies and finds the one matching our client reference ID.
     * @param clientRefId The unique identifier of the transcript to fetch.
     * @return A NetworkResult containing the Transcript object if found, or an error otherwise.
     */
    @Suppress("GraphQLUnresolvedReference")
    suspend fun getTranscript(clientRefId: String): NetworkResult<Transcript> {
        return try {
            val authHeader = "Bearer ${BuildConfig.FIREFLIES_API_KEY}"
            // Define the GraphQL query to get all user transcripts.
            val query = """
                query getMyTranscripts {
                  transcripts(mine: true) {
                    id
                    title
                    summary { overview action_items keywords outline }
                    sentences { raw_text }
                  }
                }
            """.trimIndent()

            val requestBody = mapOf("query" to query)
            val response = apiService.executeGraphQL(authHeader, requestBody)

            if (response.data != null && response.errors.isNullOrEmpty()) {
                val responseData = gson.fromJson(response.data.toString(), GetTranscriptsResponseData::class.java)

                // CRITICAL: We find our specific transcript by matching the title we assigned during upload.
                val targetTitle = clientRefId.substringBeforeLast('.')
                val ourTranscript = responseData?.transcripts?.find { it.title == targetTitle }

                if (ourTranscript != null) {
                    NetworkResult.Success(ourTranscript)
                } else {
                    // This is not a final error, but indicates the backend is still processing.
                    NetworkResult.Failure(null, "Transcription not yet available.")
                }
            } else {
                val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown API error."
                NetworkResult.Failure(null, errorMessage)
            }
        } catch (e: Exception) {
            NetworkResult.Failure(e, "An unexpected error occurred: ${e.message}")
        }
    }
}
