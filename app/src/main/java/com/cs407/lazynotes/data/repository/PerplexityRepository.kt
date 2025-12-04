package com.cs407.lazynotes.data.repository

import com.cs407.lazynotes.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class PerplexityRepository {
    private val client = OkHttpClient()
    private val apiKey = BuildConfig.PERPLEXITY_API_KEY

    suspend fun generateSummary(transcript: String): NetworkResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Please provide a bullet point summary of the following audio transcript. 
                    Focus on the main points and key information discussed.
                    
                    Transcript:
                    $transcript
                """.trimIndent()

                val json = JSONObject().apply {
                    put("model", "sonar")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                    put("max_tokens", 500)
                    put("temperature", 0.2)
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://api.perplexity.ai/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val summary = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    NetworkResult.Success(summary)
                } else {
                    NetworkResult.Failure(null, "Failed to generate summary: ${response.code}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                NetworkResult.Failure(e, "Error generating summary: ${e.message}")
            }
        }
    }
}