package com.example.bimu.data.models

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class Message : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    @PersistedName("chatroomId")
    var chatroomId: String = ""

    @PersistedName("senderId")
    var senderId: String = ""

    @PersistedName("content")
    var content: String = ""

    @PersistedName("timestamp")
    var timestamp: RealmInstant = RealmInstant.now()
}