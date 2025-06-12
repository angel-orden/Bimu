package com.example.bimu.data.models

data class RouteSearchParams(
    val fromDate: String? = null,       // ISO8601 string
    val toDate: String? = null,         // ISO8601 string
    val location: GeoPoint? = null,
    val difficulty: Int? = null
)
