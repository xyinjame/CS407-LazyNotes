package com.cs407.lazynotes.data.network.models

import com.google.gson.annotations.SerializedName

data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any>? = null
)

data class CreateTranscriptResponse(
    val id: String,
    val title: String
)

data class TranscriptResult(
    @SerializedName("createTranscript")
    val createTranscript: CreateTranscriptResponse?
)


data class GraphQLResponse(
    val data: TranscriptResult?,
    val errors: List<Any>? = null
)