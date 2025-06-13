package com.example.bimu.data.models

//import java.util.UUID
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val id: String = "",
    val chatroomId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)