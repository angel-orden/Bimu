package com.example.bimu.data.dao

import com.example.bimu.data.models.Message
import retrofit2.Response
import retrofit2.http.*

interface MessageApi {
    @POST("messages") suspend fun createMessage(@Body message: Message): Response<Message>
    @GET("messages") suspend fun getAllMessages(): Response<List<Message>>
    @GET("messages/{id}") suspend fun getMessageById(@Path("id") id: String): Response<Message>
    @DELETE("messages/{id}") suspend fun deleteMessageById(@Path("id") id: String): Response<Unit>
}

class MessageDAO(private val api: MessageApi) {
    suspend fun insert(message: Message): Message? = api.createMessage(message).body()
    suspend fun getAll(): List<Message> = api.getAllMessages().body() ?: emptyList()
    suspend fun getById(id: String): Message? = api.getMessageById(id).body()
    suspend fun deleteById(id: String): Boolean = api.deleteMessageById(id).isSuccessful
}