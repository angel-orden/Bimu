package com.example.bimu.data.models

data class User(
    val _id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,   // ahora sí, necesario para login
    val age: Int? = null,
    val gender: String? = null,
    val level: String? = null,
    val country: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val centralPoint: GeoPoint? = null,
    val radius: Double? = null
)