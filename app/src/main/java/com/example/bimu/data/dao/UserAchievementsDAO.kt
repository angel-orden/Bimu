package com.example.bimu.data.dao

import com.example.bimu.data.models.Achievement
import com.example.bimu.data.models.User
import com.example.bimu.data.models.UserAchievements
import retrofit2.Response
import retrofit2.http.*

interface UserAchievementsApi {
    @POST("userAchievements") suspend fun addUserAchievement(@Body ua: UserAchievements): Response<UserAchievements>
    @DELETE("userAchievements/{id}") suspend fun deleteUserAchievement(@Path("id") id: String): Response<Unit>
    @GET("userAchievements/user/{userId}") suspend fun getUserAchievements(@Path("userId") userId: String): Response<List<Achievement>>
}

class UserAchievementsDAO(private val api: UserAchievementsApi) {
    suspend fun add(userAchievements: UserAchievements): UserAchievements? =
        api.addUserAchievement(userAchievements).body()
    suspend fun remove(id: String): Boolean =
        api.deleteUserAchievement(id).isSuccessful
    suspend fun getAchievementsForUser(userId: String): List<Achievement> =
        api.getUserAchievements(userId).body() ?: emptyList()
}