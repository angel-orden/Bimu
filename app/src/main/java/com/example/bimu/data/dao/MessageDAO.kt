package com.example.bimu.data.dao

import com.example.bimu.data.models.Message
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageDAO(private val realm: Realm) {
    suspend fun insert(message: Message) {
        realm.write { copyToRealm(message) }
    }

    fun getAll(): Flow<List<Message>> {
        return realm.query<Message>().asFlow().map { it.list }
    }

    suspend fun getById(id: String): Message? {
        return realm.query<Message>("id == $0", id).first().find()
    }

    suspend fun deleteById(id: String) {
        realm.write {
            val msg = query<Message>("id == $0", id).first().find()
            msg?.let { delete(it) }
        }
    }
}