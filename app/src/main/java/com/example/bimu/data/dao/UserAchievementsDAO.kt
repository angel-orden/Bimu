package com.example.bimu.data.dao

import io.realm.kotlin.Realm
import java.util.Date
import com.example.bimu.data.models.Achievement
import com.example.bimu.data.models.User
import com.example.bimu.data.models.UserAchievements
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserAchievementsDAO (private val realm: Realm) {
    suspend fun add(user: User, achievement: Achievement) {
        val userAchievement = UserAchievements().apply {
            userId = user.id
            achievementId = achievement.id
        }

        realm.write {
            copyToRealm(userAchievement)
        }
    }

    suspend fun remove(user: User, achievement: Achievement) {
        realm.write {
            val result = query<UserAchievements>("userId == $0 AND achievementId == $1", user.id, achievement.id).first().find()
            result?.let { delete(it) }
        }
    }

    fun getAchievementsForUser(userId: String): Flow<List<Achievement>> {
        return realm.query<UserAchievements>("userId == $0", userId)
            .asFlow()
            .map { results ->
                val achievementIds = results.list.map { it.achievementId }
                achievementIds.mapNotNull { id ->
                    realm.query<Achievement>("_id == $0", id).first().find()
                }
            }
    }
}