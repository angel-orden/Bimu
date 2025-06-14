package com.example.bimu.data.dao

import com.example.bimu.data.models.Outing
import retrofit2.Response
import retrofit2.http.*

interface OutingApi {
    @POST("addOuting")
    suspend fun addOuting(@Body outing: Outing): Response<Outing>

    @DELETE("deleteOuting/{outingId}")
    suspend fun deleteOuting(@Path("outingId") outingId: String): Response<Unit>

    @GET("getRoutesByUser/{userId}")
    suspend fun getRoutesByUserId(@Path("userId") userId: String): Response<List<String>>

    @GET("getUsersByRoute/{routeId}")
    suspend fun getUsersByRouteId(@Path("routeId") routeId: String): Response<List<String>>

    @GET("getUserOuting/{userId}/{routeId}")
    suspend fun getUserOuting(@Path("userId") userId: String, @Path("routeId") routeId: String): Response<Outing?>
}

class OutingDAO(private val api: OutingApi) {
    suspend fun addOuting(outing: Outing): Outing? = api.addOuting(outing).body()
    suspend fun deleteOuting(outingId: String): Boolean = api.deleteOuting(outingId).isSuccessful
    suspend fun getRoutesByUserId(userId: String): List<String> = api.getRoutesByUserId(userId).body() ?: emptyList()
    suspend fun getUsersByRouteId(routeId: String): List<String> = api.getUsersByRouteId(routeId).body() ?: emptyList()
    suspend fun getUserOuting(userId: String, routeId: String): Outing? { return api.getUserOuting(userId, routeId).body() }
}