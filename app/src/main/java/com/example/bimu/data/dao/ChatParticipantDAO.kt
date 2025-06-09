package com.example.bimu.data.dao

import com.example.bimu.data.models.ChatParticipant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

class ChatParticipantDAO(private val realm: Realm) {

    suspend fun addParticipant(chatRoomId: String, userId: String) {
        realm.write {
            copyToRealm(ChatParticipant().apply {
                this.chatroomId = chatRoomId
                this.userId = userId
            })
        }
    }

    suspend fun removeParticipant(chatRoomId: String, userId: String) {
        realm.write {
            val participant = query<ChatParticipant>(
                "chatRoomId == $0 AND userId == $1",
                chatRoomId,
                userId
            ).first().find()

            participant?.let { delete(it) }
        }
    }

    fun getUsersInChat(chatRoomId: String): Flow<List<String>> {
        return realm.query<ChatParticipant>("chatRoomId == $0", chatRoomId)
            .asFlow()
            .map { results -> results.list.map { it.userId } }
    }

    fun getChatsForUser(userId: String): Flow<List<String>> {
        return realm.query<ChatParticipant>("userId == $0", userId)
            .asFlow()
            .map { results -> results.list.map { it.chatroomId } }
    }
}