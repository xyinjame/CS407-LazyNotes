package com.cs407.lazynotes.flashcards

import com.cs407.lazynotes.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PerplexityFlashcardGenerator(

) : FlashcardGenerator {
    private val apiKey = BuildConfig.PERPLEXITY_API_KEY
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json".toMediaType()

    override suspend fun generateFlashcards(transcript: String): List<Flashcard> =
        withContext(Dispatchers.IO) {
            val clippedTranscript = if (transcript.length > 2000) {
                transcript.take(2000)
            } else {
                transcript
            }

            val prompt = """
                You are an assistant that generates study flashcards for college students.

                Given the following lecture transcript, create AT MOST 5 short Q/A flashcards.

                Respond ONLY in this exact plain text format, with no extra text, no markdown, no explanations:

                Q: first question here
                A: first answer here
                ---
                Q: second question here
                A: second answer here
                ---
                (continue like this for up to 5 cards)

                Do NOT include backticks.
                Do NOT include any JSON or code fences.

                Transcript:
                $clippedTranscript
            """.trimIndent()


            val requestJson = JSONObject().apply {
                put("model", "sonar-pro")
                put("max_tokens", 400)

                val messages = org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You generate concise flashcards for students based on transcripts.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                put("messages", messages)
            }

            val body = requestJson.toString().toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url("https://api.perplexity.ai/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val code = response.code
                val errorBody = response.body?.string()
                response.close()
                throw IllegalStateException("Perplexity error $code: $errorBody")
            }

            val raw = response.body?.string().orEmpty()
            response.close()

            parseFlashcardsFromPerplexity(raw)
        }

    private fun parseFlashcardsFromPerplexity(raw: String): List<Flashcard> {
        val root = JSONObject(raw)
        val choices = root.getJSONArray("choices")
        if (choices.length() == 0) return emptyList()

        var content = choices
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()

        // Split into card blocks separated by ---
        val blocks = content.split("---")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val result = mutableListOf<Flashcard>()

        for (block in blocks) {
            val lines = block.lines().map { it.trim() }

            val qLine = lines.firstOrNull { it.startsWith("Q:", ignoreCase = true) }
            val aLine = lines.firstOrNull { it.startsWith("A:", ignoreCase = true) }

            if (qLine != null && aLine != null) {
                val question = qLine.removePrefix("Q:").trim()
                val answer = aLine.removePrefix("A:").trim()
                result += Flashcard(question, answer)
            }
        }

        return result.take(5)
    }
}
