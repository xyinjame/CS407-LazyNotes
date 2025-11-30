package com.cs407.lazynotes.data.network.models

import com.google.gson.JsonElement

// Request body for any GraphQL query or mutation
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any>? = null
)

// Represents a single error object returned by the GraphQL API
data class GraphQLError(
    val message: String
    // You can add other fields like 'locations' or 'path' if needed
)

// The generic top-level response for ANY GraphQL call.
// 'data' is a generic JsonElement that we can inspect later.
data class GraphQLResponse(
    val data: JsonElement?,
    val errors: List<GraphQLError>? = null
)
