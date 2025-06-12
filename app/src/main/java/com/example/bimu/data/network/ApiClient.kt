package com.example.bimu.data.network

import com.example.bimu.data.dao.OutingApi
import com.example.bimu.data.dao.RouteApi
import com.example.bimu.data.dao.UserApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    private val BASE_URL = "bimubackend-production.up.railway.app" // pon aquí tu url de Railway

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    // Instancias de las interfaces API
    val userApi: UserApi = retrofit.create(UserApi::class.java)
    val routeApi: RouteApi = retrofit.create(RouteApi::class.java)
    val outingApi: OutingApi = retrofit.create(OutingApi::class.java)
}