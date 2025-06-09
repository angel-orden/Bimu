package com.example.bimu.data.dao

import com.example.bimu.data.models.Route
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RouteDAO(private val realm: Realm) {
    suspend fun insert(route: Route) {
        realm.write { copyToRealm(route) }
    }

    fun getAll(): Flow<List<Route>> {
        return realm.query<Route>().asFlow().map { it.list }
    }

    suspend fun getById(id: String): Route? {
        return realm.query<Route>("id == $0", id).first().find()
    }

    suspend fun deleteById(id: String) {
        realm.write {
            val route = query<Route>("id == $0", id).first().find()
            route?.let { delete(it) }
        }
    }
}