package com.example.bimu.data.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bimu.R
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val scope = MainScope()
    private val appID = "bimu-app-wyaznyg"
    private val app = App.create(AppConfiguration.Builder(appID).build())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        // Datos de prueba
//        val email = "angel@bimu.com"
//        val password = "BimuSegura123"
//
//        scope.launch {
//            try {
//                // Registro (solo la primera vez, si se reusa da error porque existe usuario con esa credencial)
//                app.emailPasswordAuth.registerUser(email, password)
//                println("Usuario registrado correctamente")
//
//                // Login con email/password
//                val user = app.login(Credentials.emailPassword(email, password))
//                println("Login exitoso. ID del usuario en Realm: ${user.id}")
//            } catch (e: Exception) {
//                println("Error: ${e.message}")
//            }
//        }
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<Button>(R.id.registerButton).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
