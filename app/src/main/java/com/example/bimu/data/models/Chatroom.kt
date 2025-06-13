package com.example.bimu.data.models

//import java.util.UUID
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Chatroom(
    val id: String = "",
    val routeId: String = "",
    val isPrivate: Boolean = false
)