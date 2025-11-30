package com.cs407.lazynotes.data.repository

import com.cs407.lazynotes.BuildConfig
import com.cs407.lazynotes.data.network.FirefliesService
import com.cs407.lazynotes.data.storage.StorageService
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Failure(val error: Throwable?, val message: String) : NetworkResult<Nothing>()
}

data class TranscriptSummary(
    val overview: String?,
    @SerializedName("action_items")
    val actionItems: List<String>?,
    val keywords: List<String>?,
    val outline: List<String>?
)

private data class UploadAudioResponse(val uploadAudio: UploadAudioPayload?)
private data class UploadAudioPayload(val success: Boolean, val message: String?)

// Revert to the list response data class
private data class GetTranscriptsResponseData(val transcripts: List<Transcript>?)

data class Transcript(val id: String?, val title: String?, val summary: TranscriptSummary?, val sentences: List<Sentence>?)
data class Sentence(val raw_text: String?)

class FirefliesRepository(
    private val apiService: FirefliesService,
    private val storageService: StorageService
) {
    private val gson = Gson()

    @Suppress("GraphQLUnresolvedReference")
    suspend fun processRecordingForTranscription(localFile: File, title: String): NetworkResult<String> {
        return try {
            val publicAudioUrl = storageService.uploadAudioFile(localFile)
            if (publicAudioUrl.isNullOrEmpty()) {
                return NetworkResult.Failure(null, "Firebase Storage Upload failed, URL is empty.")
            }
            println("Firebase Public URL: $publicAudioUrl")

            val authHeader = "Bearer ${BuildConfig.FIREFLIES_API_KEY}"
            val clientRefId = localFile.name

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
            val response = apiService.executeGraphQL(authHeader, requestBody)

            if (response.data != null && response.errors.isNullOrEmpty()) {
                val uploadResponse = gson.fromJson(response.data, UploadAudioResponse::class.java)
                if (uploadResponse.uploadAudio?.success == true) {
                    NetworkResult.Success(clientRefId)
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

    @Suppress("GraphQLUnresolvedReference")
    suspend fun getTranscript(clientRefId: String): NetworkResult<Transcript> {
        return try {
            val authHeader = "Bearer ${BuildConfig.FIREFLIES_API_KEY}"
            // Revert to the plural 'transcripts(mine: true)' query
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

            // This query has no variables
            val requestBody = mapOf("query" to query)
            val response = apiService.executeGraphQL(authHeader, requestBody)

            if (response.data != null && response.errors.isNullOrEmpty()) {
                // Parse the list response
                val responseData = gson.fromJson(response.data, GetTranscriptsResponseData::class.java)

                // Find our specific transcript in the list by title
                val targetTitle = clientRefId.substringBeforeLast('.')
                val ourTranscript = responseData?.transcripts?.find { it.title == targetTitle }

                if (ourTranscript != null) {
                    NetworkResult.Success(ourTranscript)
                } else {
                    // This means our transcript hasn't appeared in the list yet.
                    NetworkResult.Failure(null, "Transcription not yet in the list.")
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