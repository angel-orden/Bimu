package com.example.bimu.data.dao

import com.example.bimu.data.models.Achievement
import retrofit2.Response
import retrofit2.http.*


interface AchievementApi {
    @POST("achievements") suspend fun createAchievement(@Body achievement: Achievement): Response<Achievement>
    @GET("achievements") suspend fun getAllAchievements(): Response<List<Achievement>>
    @GET("achievements/{id}") suspend fun getAchievementById(@Path("id") id: String): Response<Achievement>
    @DELETE("achievements/{id}") suspend fun deleteAchievementById(@Path("id") id: String): Response<Unit>
}

class AchievementDAO(private val api: AchievementApi) {
    suspend fun insert(achievement: Achievement) = api.createAchievement(achievement).body()
    suspend fun getAll(): List<Achievement> = api.getAllAchievements().body() ?: emptyList()
    suspend fun getById(id: String): Achievement? = api.getAchievementById(id).body()
    suspend fun deleteById(id: String): Boolean = api.deleteAchievementById(id).isSuccessful
}