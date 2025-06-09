package com.example.bimu.data.models

import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.annotations.PersistedName
import kotlin.math.abs

class GeoPoint(latitude: Double, longitude: Double) : EmbeddedRealmObject {

    @PersistedName("latitude")
    var latitude: Double = 0.0

    @PersistedName("longitude")
    var longitude: Double = 0.0

    @PersistedName("timestamp")
    var timestamp: RealmInstant = RealmInstant.now()

    override fun toString(): String {
        return "($latitude, $longitude)"
    }

    fun isValid(): Boolean {
        return abs(latitude) <= 90 && abs(longitude) <= 180
    }
}