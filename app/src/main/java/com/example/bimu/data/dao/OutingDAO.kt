package com.example.bimu.data.dao

import com.example.bimu.data.models.Outing
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OutingDAO(private val realm: Realm) {
    suspend fun insert(outing: Outing) {
        realm.write { copyToRealm(outing) }
    }

    fun getAll(): Flow<List<Outing>> {
        return realm.query<Outing>().asFlow().map { it.list }
    }

    suspend fun getById(id: String): Outing? {
        return realm.query<Outing>("id == $0", id).first().find()
    }

    suspend fun deleteById(id: String) {
        realm.write {
            val outing = query<Outing>("id == $0", id).first().find()
            outing?.let { delete(it) }
        }
    }
}