package com.example.bimu.data.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bimu.R
import com.example.bimu.data.models.User
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val scope = MainScope()
    private val appID = "bimu-app-wyaznyg"
    private val app = App.create(AppConfiguration.Builder(appID).build())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.loginBtn).setOnClickListener {
            // Lógica para iniciar sesión
            val mail = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            // Aquí puedes implementar la lógica para autenticar al usuario

            if (mail.isEmpty() || password.isEmpty()) {
                // Mostrar mensaje de error
                Toast.makeText(this, "Todos los campos deben rellenarse", Toast.LENGTH_SHORT).show()
            } else {
                // Manda el login a la base de datos y pregunta por las credenciales
                scope.launch {
                    val result = loginUser(mail, password)
                    if (result.isSuccess) {
                        // Login exitoso
                        val user = result.getOrNull()
                        Toast.makeText(this@LoginActivity, "Login exitoso: ${user?.username}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    } else {
                        val err = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                        Toast.makeText(this@LoginActivity, "Error: $err", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findViewById<Button>(R.id.registerBtnLog).setOnClickListener {
            // Lógica para navegar a la pantalla de registro
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val credentials = Credentials.emailPassword(email, password)
            val realmUser = app.login(credentials)
            //Abrir Realm sync y buscar al usuario por id
            val config = SyncConfiguration.Builder(realmUser, setOf(User::class)).build()
            val realm = Realm.open(config)
            val user = realm.query<User>("id == $0", realmUser.id).first().find()
            if (user!= null) {
                Result.success(user)
            } else {
                Result.failure(Exception("No user profile found for this account"))
            }


        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}