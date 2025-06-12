package com.example.bimu.data.dao

import com.example.bimu.data.models.User
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @POST("registerUser")
    suspend fun registerUser(@Body user: User): Response<User>

    @PUT("editUser/{id}")
    suspend fun editUser(@Path("id") id: String, @Body fields: Map<String, @JvmSuppressWildcards Any>): Response<User>

    @DELETE("deleteUser/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>

    @GET("findUserByUsername/{username}")
    suspend fun findUserByUsername(@Path("username") username: String): Response<List<User>>

    @GET("getUser/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<User>

    @POST("login")
    suspend fun login(@Body loginRequest: Map<String, String>): Response<User>
}

class UserDAO(private val api: UserApi) {
    suspend fun registerUser(user: User): User? = api.registerUser(user).body()
    suspend fun editUser(id: String, fields: Map<String, Any>): User? = api.editUser(id, fields).body()
    suspend fun deleteUser(id: String): Boolean = api.deleteUser(id).isSuccessful
    suspend fun findUserByUsername(username: String): List<User> = api.findUserByUsername(username).body() ?: emptyList()
    suspend fun getUserById(id: String): User? = api.getUserById(id).body()
    suspend fun login(email: String, password: String): User? =
        api.login(mapOf("email" to email, "password" to password)).body()
}