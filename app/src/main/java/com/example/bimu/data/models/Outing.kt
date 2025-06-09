package com.example.bimu.data.models

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class Outing : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    @PersistedName("userId")
    var userId: String = ""

    @PersistedName("routeId")
    var routeId: String = ""

    @PersistedName("completed")
    var completed: Boolean = false

    @PersistedName("notes")
    var notes: String? = null // Puede ser nulo

    @PersistedName("joinedAt")
    var joinedAt: RealmInstant = RealmInstant.now()

}