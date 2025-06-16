package com.example.bimu.data.models

import kotlin.math.abs
import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONObject

@JsonClass(generateAdapter = true)
class GeoPoint() {

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    constructor(latitude: Double, longitude: Double): this(){
        this.latitude = latitude
        this.longitude = longitude
    }

    override fun toString(): String {
        return "($latitude, $longitude)"
    }

    fun isValid(): Boolean {
        return abs(latitude) <= 90 && abs(longitude) <= 180
    }

    // Para enviar a backend
    fun toGeoJson(): JSONObject = JSONObject().apply {
        put("type", "Point")
        put("coordinates", JSONArray().apply {
            put(longitude) // OJO: primero long, luego lat (GeoJSON)
            put(latitude)
        })
    }

    companion object {
        // Para leer del backend
        fun fromGeoJson(json: JSONObject): GeoPoint {
            val coords = json.getJSONArray("coordinates")
            return GeoPoint(
                latitude = coords.getDouble(1),
                longitude = coords.getDouble(0)
            )
        }
    }
}