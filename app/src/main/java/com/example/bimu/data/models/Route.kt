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
){
    fun toRequestMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "title" to title,
            "description" to description,
            "creatorId" to creatorId,
            "distanceKm" to distanceKm,
            "difficulty" to difficulty,
            "timeStart" to timeStart
        )
        // Solo añade locationStart si no es nulo
        locationStart?.let {
            map["locationStart"] = it.toGeoJson()
        }
        // Añade _id si está presente (para editar)
        _id?.let {
            map["_id"] = it
        }
        return map
    }
}