package com.cs407.lazynotes.data.network

import com.cs407.lazynotes.data.network.models.GraphQLResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface FirefliesService {

    @Headers("Content-Type: application/json")
    @POST("graphql")
    suspend fun executeGraphQL(
        @Header("Authorization") authorization: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): GraphQLResponse
}