package com.example.bimu.data.dao

import com.example.bimu.data.models.Achievement
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AchievementDAO(private val realm: Realm) {
    suspend fun insert(achievement: Achievement) {
        realm.write { copyToRealm(achievement) }
    }

    fun getAll(): Flow<List<Achievement>> {
        return realm.query<Achievement>().asFlow().map { it.list }
    }

    suspend fun getById(id: String): Achievement? {
        return realm.query<Achievement>("id == $0", id).first().find()
    }

    suspend fun deleteById(id: String) {
        realm.write {
            val ach = query<Achievement>("id == $0", id).first().find()
            ach?.let { delete(it) }
        }
    }
}