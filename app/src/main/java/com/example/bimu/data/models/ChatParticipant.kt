package com.example.bimu.data.models

//import java.util.UUID
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatParticipant(
    val id: String = "",
    val userId: String = "",
    val chatroomId: String = ""
)