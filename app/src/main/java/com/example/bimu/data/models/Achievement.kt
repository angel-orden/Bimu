package com.example.bimu.data.models

//import java.util.UUID por si en un futuro queremos generar las ids nosotros
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Achievement(
    val id: String = "",
    val title: String = "",
    val description: String = ""
)