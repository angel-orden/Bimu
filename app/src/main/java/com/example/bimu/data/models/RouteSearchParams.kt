package com.example.bimu.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RouteSearchParams(
    val fromDate: String? = null,       // ISO8601 string
    val toDate: String? = null,         // ISO8601 string
    val location: GeoPoint? = null,
    val radiusKm: Double? = null,
    val difficulty: String? = null
)
