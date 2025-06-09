package com.example.bimu.data.dao

import com.example.bimu.data.models.Achievement
import com.example.bimu.data.models.User
import com.example.bimu.data.models.UserAchievements
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserDAO(private val realm: Realm) {
    suspend fun insert(user: User) {
        realm.write { copyToRealm(user) }
    }

    fun getAll(): Flow<List<User>> {
        return realm.query<User>().asFlow().map { it.list }
    }

    suspend fun getById(id: String): User? {
        return realm.query<User>("id == $0", id).first().find()
    }

    suspend fun deleteById(id: String) {
        realm.write {
            val user = query<User>("id == $0", id).first().find()
            user?.let { delete(it) }
        }
    }
    suspend fun update(user: User): Boolean {

        var success = false
        realm.write {
            val existingUser = query<User>("id == $0", user.id).first().find()
            existingUser?.let {
                it.username = user.username
                it.email = user.email
                it.age = user.age
                it.gender = user.gender
                it.country = user.country
                it.bio = user.bio
                it.avatarUrl = user.avatarUrl
                it.centralPoint = user.centralPoint
                it.radius = user.radius
                success = true
            }
        }
        return success
    }

    suspend fun getAchievements(user: User): List<Achievement> {
        // 1. Obtener las IDs de los logros del usuario usando la tabla intermedia
        val achievementIds = realm.query<UserAchievements>("userId == $0", user.id)
            .find()
            .map { it.achievementId }

        // 2. Si no hay logros, devolver lista vacía
        if (achievementIds.isEmpty()) return emptyList()

        // 3. Buscar los logros con esas IDs
        return realm.query<Achievement>("_id IN $0", achievementIds)
            .find()
    }

}