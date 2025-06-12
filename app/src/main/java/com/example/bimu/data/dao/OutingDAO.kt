package com.example.bimu.data.dao

import com.example.bimu.data.models.Outing
import retrofit2.Response
import retrofit2.http.*

interface OutingApi {
    @POST("addOuting")
    suspend fun addOuting(@Body outing: Outing): Response<Outing>

    @DELETE("deleteOuting/{outingId}")
    suspend fun deleteOuting(@Path("outingId") outingId: String): Response<Unit>

    @GET("findOutingsByUser/{userId}")
    suspend fun findOutingsByUserId(@Path("userId") userId: String): Response<List<Outing>>

    @GET("findOutingsByRoute/{routeId}")
    suspend fun findOutingsByRouteId(@Path("routeId") routeId: String): Response<List<Outing>>
}

class OutingDAO(private val api: OutingApi) {
    suspend fun addOuting(outing: Outing): Outing? = api.addOuting(outing).body()
    suspend fun deleteOuting(outingId: String): Boolean = api.deleteOuting(outingId).isSuccessful
    suspend fun findOutingsByUserId(userId: String): List<Outing> = api.findOutingsByUserId(userId).body() ?: emptyList()
    suspend fun findOutingsByRouteId(routeId: String): List<Outing> = api.findOutingsByRouteId(routeId).body() ?: emptyList()
}