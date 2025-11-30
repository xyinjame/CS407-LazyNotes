package com.cs407.lazynotes.data.network.models

import com.google.gson.JsonElement

/**
 * Represents a standard GraphQL error object.
 */
data class GraphQLError(
    val message: String?,
    val locations: List<Map<String, Int>>? = null,
    val path: List<Any>? = null,
    val extensions: Map<String, Any>? = null
)

/**
 * A generic wrapper for a GraphQL response that can hold any type of `data`.
 * The `errors` list is now correctly typed to `GraphQLError`.
 */
data class GraphQLResponse(
    val data: JsonElement?,
    val errors: List<GraphQLError>?
)
