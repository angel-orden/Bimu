package com.example.bimu.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class Chatroom : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    @PersistedName("routeId")
    var routeId: String = ""

    @PersistedName("isPrivate")
    var isPrivate: Boolean = false
}