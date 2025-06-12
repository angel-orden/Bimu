package com.example.bimu.data.dao

import com.example.bimu.data.models.ChatParticipant
import retrofit2.Response
import retrofit2.http.*

interface ChatParticipantApi {
    @POST("chatParticipants") suspend fun addParticipant(@Body participant: ChatParticipant): Response<ChatParticipant>
    @DELETE("chatParticipants/{id}") suspend fun removeParticipant(@Path("id") id: String): Response<Unit>
    @GET("chatParticipants/chat/{chatroomId}") suspend fun getUsersInChat(@Path("chatroomId") chatroomId: String): Response<List<String>>
    @GET("chatParticipants/user/{userId}") suspend fun getChatsForUser(@Path("userId") userId: String): Response<List<String>>
}

class ChatParticipantDAO(private val api: ChatParticipantApi) {
    suspend fun addParticipant(participant: ChatParticipant): ChatParticipant? =
        api.addParticipant(participant).body()
    suspend fun removeParticipant(id: String): Boolean =
        api.removeParticipant(id).isSuccessful
    suspend fun getUsersInChat(chatroomId: String): List<String> =
        api.getUsersInChat(chatroomId).body() ?: emptyList()
    suspend fun getChatsForUser(userId: String): List<String> =
        api.getChatsForUser(userId).body() ?: emptyList()
}