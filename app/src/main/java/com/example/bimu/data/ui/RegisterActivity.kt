package com.example.bimu.data.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.bimu.R
import com.example.bimu.data.models.User
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private val scope = MainScope()
    private val appID = "bimu-app-wyaznyg"
    private val app = App.create(AppConfiguration.Builder(appID).build())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        findViewById<Button>(R.id.registerBtn).setOnClickListener {
            // Lógica para registrar usuario
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            val userNamer = findViewById<EditText>(R.id.nameEditText).text.toString()
            // Aquí puedes implementar la lógica para registrar al usuario

            if (email.isEmpty() || password.isEmpty() || userNamer.isEmpty()) {
                // Mostrar mensaje de error
                Toast.makeText(this, "Todos los campos deben rellenarse", Toast.LENGTH_SHORT).show()
            } else {

                lifecycleScope.launch {
                    val result = registerUser(email, password, userNamer)
                    if (result.isSuccess) {
                        // Registro exitoso
                        val user = result.getOrNull()
                        Toast.makeText(this@RegisterActivity, "Usuario registrado: ${user?.username}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                        intent.putExtra("fromRegister", true)
                        startActivity(intent)
                        finish()
                    } else {
                        val err = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                        Toast.makeText(this@RegisterActivity, "Error: $err", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findViewById<Button>(R.id.loginBtnReg).setOnClickListener {
            // Lógica para navegar a la pantalla de inicio de sesión
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    suspend fun registerUser(email: String, password: String, username: String): Result<User> {
        return try {
            app.emailPasswordAuth.registerUser(email, password)
            // Registro exitoso
            val credentials = Credentials.emailPassword(email, password)
            val realmUser = app.login(credentials)

            val userObj = User().apply {
                id = realmUser.id
                this.username = username
                this.email = email
            }
            val realm = Realm.open(RealmConfiguration.Builder(schema = setOf(User::class)).build())
            realm.write {
                copyToRealm(userObj)
            }
            Result.success(userObj)

        } catch (e: Exception) {
            // Manejar errores de registro
            Result.failure(e)
        }
    }
}