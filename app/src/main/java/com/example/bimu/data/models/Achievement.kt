package com.example.bimu.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class Achievement : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    @PersistedName("title")
    var title: String = ""

    @PersistedName("description")
    var description: String = ""
}