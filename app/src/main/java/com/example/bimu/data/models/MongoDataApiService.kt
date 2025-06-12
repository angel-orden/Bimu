package com.example.bimu.data.models

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class FindOneBody(
    val dataSource: String,
    val database: String,
    val collection: String,
    val filter: Map<String, Any>
)
data class FindResponse<T>(val document: T?)
data class InsertOneBody<T>(
    val dataSource: String,
    val database: String,
    val collection: String,
    val document: T
)
data class InsertOneResponse(val insertedId: String)

interface MongoDataApiService {
    @POST("action/findOne")
    suspend fun findOne(
        @Body body: FindOneBody,
        @Header("api-key") apiKey: String
    ): Response<FindResponse<User>>

    @POST("action/insertOne")
    suspend fun insertOne(
        @Body body: InsertOneBody<User>,
        @Header("api-key") apiKey: String
    ): Response<InsertOneResponse>
}