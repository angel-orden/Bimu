package com.example.bimu.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserAchievements(
    val id: String = "",
    val userId: String = "",
    val achievementId: String = "",
    val achievedAt: Long = System.currentTimeMillis() // Epoch millis
)