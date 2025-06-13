package com.example.bimu.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Outing(
    val _id: String? = null,         // ObjectId como string
    val completed: Boolean = false,
    val joinedAt: String? = null,    // Fecha/hora ISO8601
    val notes: String? = null,
    val routeId: String,             // _id de la ruta (string)
    val userId: String               // _id del usuario (string)
)
