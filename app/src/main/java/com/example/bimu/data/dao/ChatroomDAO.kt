package com.example.bimu.data.dao

import com.example.bimu.data.models.Chatroom
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatroomDAO(private val realm: Realm) {
    suspend fun insert(chatroom: Chatroom) {
        realm.write { copyToRealm(chatroom) }
    }

    fun getAll(): Flow<List<Chatroom>> {
        return realm.query<Chatroom>().asFlow().map { it.list }
    }

    suspend fun getById(id: String): Chatroom? {
        return realm.query<Chatroom>("id == $0", id).first().find()
    }

    suspend fun deleteById(id: String) {
        realm.write {
            val chat = query<Chatroom>("id == $0", id).first().find()
            chat?.let { delete(it) }
        }
    }
}