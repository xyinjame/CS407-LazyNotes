package com.cs407.lazynotes.data.repository

import com.cs407.lazynotes.BuildConfig
import com.cs407.lazynotes.data.network.FirefliesService
import com.cs407.lazynotes.data.network.models.GraphQLRequest

class FirefliesRepository(
    private val apiService: FirefliesService
) {

    private val API_KEY = BuildConfig.FIREFLIES_API_KEY

    /**
     *  Fireflies API transcription
     * @param title Title of the Note。
     * @param publicAudioUrl Link of public accessible recording file。
     * @return ID sucessfully created, null if FAILED。
     */
    suspend fun createNoteFromAudio(title: String, publicAudioUrl: String): String? {

        val authHeader = "Bearer $API_KEY"

        val mutation = """
            mutation {
                createTranscript(input: { 
                    title: "$title", 
                    audio_url: "$publicAudioUrl" 
                }) {
                    id
                    title
                }
            }
        """.trimIndent()

        val request = GraphQLRequest(query = mutation)

        return try {

            val response = apiService.executeGraphQL(authHeader, request)


            if (response.errors.isNullOrEmpty()) {
                response.data?.createTranscript?.id
            } else {

                println("Fireflies API Error: ${response.errors.joinToString { it.toString() }}")
                null
            }
        } catch (e: Exception) {

            println("Network/Parsing Error during audio upload: ${e.message}")
            null
        }
    }
}