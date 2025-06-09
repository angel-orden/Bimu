package com.example.bimu.data.models

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class UserAchievements : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    @PersistedName("userId")
    var userId: String = ""

    @PersistedName("achievementId")
    var achievementId: String = ""

    @PersistedName("achievedAt")
    var achievedAt: RealmInstant = RealmInstant.now()
}