package com.example.bimu.data.models

import android.content.Context
import androidx.core.content.edit

class AuxClass {

    fun saveUserIdToPrefs(context: Context, userId: String) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit { putString("userId", userId) }
    }

    fun getUserIdFromPrefs(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userId", null)
    }

    fun clearUserIdFromPrefs(context: Context) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit { remove("userId") }
    }
}