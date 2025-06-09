package com.example.bimu.data.models

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.realm.kotlin.types.annotations.PersistedName

class Route : RealmObject {
    @PrimaryKey
    var id: String = ""

    @PersistedName("title")
    var title: String = ""

    @PersistedName("description")
    var description: String = ""

    @PersistedName("creatorId")
    var creatorId: String = ""

    @PersistedName("distanceKm")
    var distanceKm: Double = 0.0

    @PersistedName("difficulty")
    var difficulty: Int = 0

    @PersistedName("locationStart")
    var locationStart: GeoPoint? = null

    @PersistedName("locationEnd")
    var locationEnd: GeoPoint? = null

    @PersistedName("timeStart")
    var timeStart: RealmInstant? = null

    @PersistedName("timeEnd")
    var timeEnd: RealmInstant? = null

}