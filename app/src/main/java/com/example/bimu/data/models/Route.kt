package com.example.bimu.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Route(
    val _id: String? = null,               // ObjectId como string (MongoDB)
    val title: String,
    val description: String? = null,
    val creatorId: String,                 // authUserId del User
    val distanceKm: Double = 0.0,
    val difficulty: String? = "Novato",
    val locationStart: GeoPoint? = null,
    val locationEnd: GeoPoint? = null,
    val timeStart: String? = null,         // ISO8601 string (ej: "2025-06-13T15:00:00Z")
    val timeEnd: String? = null            // igual que arriba
)