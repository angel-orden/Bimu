package com.example.bimu.data.dao

import android.util.Log
import com.example.bimu.data.models.GeoPoint
import com.example.bimu.data.models.Route
import com.example.bimu.data.models.RouteSearchParams
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface RouteApi {
    @POST("addRoute")
    suspend fun addRoute(@Body route: Map<String, @JvmSuppressWildcards Any?>): Response<Route>

    @PUT("editRoute/{routeId}")
    suspend fun editRoute(@Path("routeId") routeId: String, @Body fields: Map<String, @JvmSuppressWildcards Any?>): Response<Route>

    @DELETE("deleteRoute/{routeId}")
    suspend fun deleteRoute(@Path("routeId") routeId: String): Response<Unit>

    @POST("searchRoutes")
    suspend fun searchRoutes(@Body params: RouteSearchParams): Response<List<Route>>

    @GET("getRouteById/{routeId}")
    suspend fun getRouteById(@Path("routeId") routeId: String): Response<Route>
}

class RouteDAO(private val api: RouteApi) {

    // Añadir ruta: enviar la localización como GeoJSON
    suspend fun addRoute(route: Route): Route? {
        val map = mutableMapOf<String, Any?>(
            "title" to route.title,
            "description" to route.description,
            "difficulty" to route.difficulty,
            "creatorId" to route.creatorId,
            "distanceKm" to route.distanceKm,
            "timeStart" to route.timeStart,
            "locationStart" to (route.locationStart?.toGeoJson() ?: JSONObject())
        )
        val response = api.addRoute(map)
        return response.body()
    }

    // Editar ruta (igual, puedes pasar solo los campos a actualizar)
    suspend fun editRoute(routeId: String, fields: Map<String, Any>): Route? {
        // Si editas locationStart, pásala como GeoJSON también:
        val fixedFields = fields.toMutableMap()
        if (fields["locationStart"] is GeoPoint) {
            fixedFields["locationStart"] = (fields["locationStart"] as GeoPoint).toGeoJson()
        }
        val response = api.editRoute(routeId, fixedFields)
        return response.body()
    }

    suspend fun deleteRoute(routeId: String): Boolean = api.deleteRoute(routeId).isSuccessful

    suspend fun searchRoutes(params: RouteSearchParams): List<Route> {
        Log.d("BIMU", "Enviando filtro: $params")
        val response = api.searchRoutes(params)
        return response.body() ?: emptyList()
    }

    // Recibir ruta del backend: asegúrate de parsear bien locationStart
    suspend fun getRouteById(routeId: String): Route? {
        val response = api.getRouteById(routeId)
        return response.body()
    }

    // Útil para parsear una ruta a mano desde JSONObject (si alguna vez lo necesitas)
    fun routeFromJson(jsonObject: JSONObject): Route {
        val locationStart: GeoPoint? = if (jsonObject.has("locationStart") && !jsonObject.isNull("locationStart")) {
            GeoPoint.fromGeoJson(jsonObject.getJSONObject("locationStart"))
        } else null

        return Route(
            _id = jsonObject.optString("_id", ""),
            title = jsonObject.optString("title", ""),
            description = jsonObject.optString("description", ""),
            creatorId = jsonObject.optString("creatorId", ""),
            distanceKm = jsonObject.optDouble("distanceKm", 0.0),
            difficulty = jsonObject.optString("difficulty", ""),
            locationStart = locationStart,
            timeStart = jsonObject.optString("timeStart", "")
        )
    }
}