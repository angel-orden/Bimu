package com.example.bimu.data.network

import com.example.bimu.data.dao.OutingApi
import com.example.bimu.data.dao.RouteApi
import com.example.bimu.data.dao.UserApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object ApiClient {
    private val BASE_URL = "https://bimubackend-production.up.railway.app/" // Corregido

    // Construye la instancia de Moshi
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // Añade soporte para clases Kotlin
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi)) // <-- Instancia de moshi
        .build()

    val userApi: UserApi = retrofit.create(UserApi::class.java)
    val routeApi: RouteApi = retrofit.create(RouteApi::class.java)
    val outingApi: OutingApi = retrofit.create(OutingApi::class.java)
}