package com.example.bimu.data.models

import kotlin.math.abs
import com.squareup.moshi.JsonClass

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
}