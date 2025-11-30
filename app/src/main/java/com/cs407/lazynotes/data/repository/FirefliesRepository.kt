package com.cs407.lazynotes.data.repository

import com.cs407.lazynotes.BuildConfig
import com.cs407.lazynotes.data.network.FirefliesService
import com.cs407.lazynotes.data.network.models.GraphQLRequest
import com.cs407.lazynotes.data.storage.StorageService
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File

/**
 * A sealed class to represent the result of a network operation, which can either be a success or a failure.
 * This provides a robust and type-safe way to handle API responses.
 */
sealed class NetworkResult<out T> {
    /**
     * Represents a successful network response.
     * @param data The data received from the network call.
     */
    data class Success<out T>(val data: T) : NetworkResult<T>()

    /**
     * Represents a failed network response.
     * @param error An optional throwable caught during the operation.
     * @param message A descriptive error message.
     */
    data class Failure(val error: Throwable?, val message: String) : NetworkResult<Nothing>()
}

/**
 * Type-safe data class representing the summary of a transcript from the Fireflies API.
 */
data class TranscriptSummary(
    val overview: String?,
    @SerializedName("action_items")
    val actionItems: List<String>?,
    val keywords: List<String>?,
    val outline: List<String>?
)

// Private helper data classes used for parsing the GraphQL response with Gson.
private data class GetTranscriptResponseData(val transcripts: List<Transcript>?)
private data class Transcript(val id: String?, val title: String?, val summary: TranscriptSummary?)


/**
 * Repository for handling all data operations related to the Fireflies.ai service.
 *
 * This class abstracts the data sources (network and storage) from the rest of the application,
 * providing a clean API for transcription and summary retrieval.
 */
class FirefliesRepository(
    private val apiService: FirefliesService,
    private val storageService: StorageService
) {

    private val gson = Gson()

    /**
     * Processes a local audio file for transcription.
     * This involves uploading the file to a public storage and then calling the Fireflies API.
     *
     * @param localFile The audio file to be transcribed.
     * @param title The title to be assigned to the transcript.
     * @return A [NetworkResult] containing the client reference ID on success, or an error on failure.
     */
    suspend fun processRecordingForTranscription(localFile: File, title: String): NetworkResult<String> {
        return try {
            // 1. Upload audio file to get a public URL.
            val publicAudioUrl = storageService.uploadAudioFile(localFile)
            if (publicAudioUrl.isNullOrEmpty()) {
                return NetworkResult.Failure(null, "Firebase Storage Upload failed, URL is empty.")
            }
            println("Firebase Public URL: $publicAudioUrl")

            // 2. Prepare and execute the GraphQL mutation to start transcription.
            val authHeader = "Bearer ${BuildConfig.FIREFLIES_API_KEY}"
            val clientRefId = localFile.name // Using file name as a unique ID

            val mutation = """
                mutation uploadAudio(${'$'}input: AudioUploadInput) { 
                    uploadAudio(input: ${'$'}input) { success title message }
                }
            """.trimIndent()

            val variables = mapOf(
                "input" to mapOf(
                    "url" to publicAudioUrl,
                    "title" to title,
                    "client_reference_id" to clientRefId,
                )
            )
            val request = GraphQLRequest(query = mutation, variables = variables)
            val response = apiService.executeGraphQL(authHeader, request)

            // 3. Handle the API response.
            if (response.data != null && response.errors.isNullOrEmpty()) {
                NetworkResult.Success(clientRefId)
            } else {
                val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown API error during transcription processing."
                NetworkResult.Failure(null, errorMessage)
            }
        } catch (e: Exception) {
            NetworkResult.Failure(e, "An unexpected error occurred: ${e.message}")
        }
    }

    /**
     * Retrieves the transcription summary by polling the Fireflies API with a client reference ID.
     *
     * @param clientRefId The unique ID that was sent during the initial transcription request.
     * @return A [NetworkResult] containing the [TranscriptSummary] on success, or an error on failure.
     *         A failure may indicate the transcript is not yet ready.
     */
    suspend fun getTranscriptSummary(clientRefId: String): NetworkResult<TranscriptSummary> {
        return try {
            // 1. Prepare and execute the GraphQL query.
            val authHeader = "Bearer ${BuildConfig.FIREFLIES_API_KEY}"
            val query = """
                query getTranscriptByClientId(${'$'}client_reference_id: String!) {
                  transcripts(where: { client_reference_id: { _eq: ${'$'}client_reference_id } }) {
                    id
                    title
                    summary { overview action_items keywords outline }
                  }
                }
            """.trimIndent()

            val variables = mapOf("client_reference_id" to clientRefId)
            val request = GraphQLRequest(query = query, variables = variables)
            val response = apiService.executeGraphQL(authHeader, request)

            // 2. Handle API response and parse data into type-safe models.
            if (response.data != null && response.errors.isNullOrEmpty()) {
                val responseData: GetTranscriptResponseData? = gson.fromJson(response.data, GetTranscriptResponseData::class.java)
                val summary = responseData?.transcripts?.firstOrNull()?.summary
                
                if (summary != null) {
                    NetworkResult.Success(summary)
                } else {
                    NetworkResult.Failure(null, "Transcription not ready or not found.")
                }
            } else {
                val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown API error while fetching summary."
                NetworkResult.Failure(null, errorMessage)
            }
        } catch (e: Exception) {
            NetworkResult.Failure(e, "An unexpected error occurred while fetching summary: ${e.message}")
        }
    }
}
