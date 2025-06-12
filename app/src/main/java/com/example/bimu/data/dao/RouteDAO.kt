package com.example.bimu.data.dao

import com.example.bimu.data.models.Route
import com.example.bimu.data.models.RouteSearchParams
import retrofit2.Response
import retrofit2.http.*

interface RouteApi {
    @POST("addRoute")
    suspend fun addRoute(@Body route: Route): Response<Route>

    @PUT("editRoute/{routeId}")
    suspend fun editRoute(@Path("routeId") routeId: String, @Body fields: Map<String, @JvmSuppressWildcards Any>): Response<Route>

    @DELETE("deleteRoute/{routeId}")
    suspend fun deleteRoute(@Path("routeId") routeId: String): Response<Unit>

    @POST("searchRoutes")
    suspend fun searchRoutes(@Body params: RouteSearchParams): Response<List<Route>>
}

class RouteDAO(private val api: RouteApi) {
    suspend fun addRoute(route: Route): Route? = api.addRoute(route).body()
    suspend fun editRoute(routeId: String, fields: Map<String, Any>): Route? = api.editRoute(routeId, fields).body()
    suspend fun deleteRoute(routeId: String): Boolean = api.deleteRoute(routeId).isSuccessful
    suspend fun searchRoutes(params: RouteSearchParams): List<Route> = api.searchRoutes(params).body() ?: emptyList()
}