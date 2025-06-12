package com.example.bimu.data.dao

import com.example.bimu.data.models.Chatroom
import retrofit2.Response
import retrofit2.http.*

interface ChatroomApi {
    @POST("chatrooms") suspend fun createChatroom(@Body chatroom: Chatroom): Response<Chatroom>
    @GET("chatrooms") suspend fun getAllChatrooms(): Response<List<Chatroom>>
    @GET("chatrooms/{id}") suspend fun getChatroomById(@Path("id") id: String): Response<Chatroom>
    @DELETE("chatrooms/{id}") suspend fun deleteChatroomById(@Path("id") id: String): Response<Unit>
}

class ChatroomDAO(private val api: ChatroomApi) {
    suspend fun insert(chatroom: Chatroom): Chatroom? = api.createChatroom(chatroom).body()
    suspend fun getAll(): List<Chatroom> = api.getAllChatrooms().body() ?: emptyList()
    suspend fun getById(id: String): Chatroom? = api.getChatroomById(id).body()
    suspend fun deleteById(id: String): Boolean = api.deleteChatroomById(id).isSuccessful
}