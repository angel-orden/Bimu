package com.example.bimu.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey

class User : RealmObject {
    @PrimaryKey
    var id: String = ""

    @PersistedName("username")
    var username: String = ""

    @PersistedName("email")
    var email: String = ""

    @PersistedName("age")
    var age: Int = 0

    @PersistedName("gender")
    var gender: String = ""

    @PersistedName("level")
    var level: String = ""

    @PersistedName("country")
    var country: String = ""

    @PersistedName("bio")
    var bio: String = ""

    @PersistedName("avatarUrl")
    var avatarUrl: String = ""

    @PersistedName("centralPoint")
    var centralPoint: GeoPoint? = null

    @PersistedName("radius")
    var radius: Double = 0.0

}