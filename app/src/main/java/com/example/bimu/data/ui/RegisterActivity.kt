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
import com.example.bimu.data.dao.UserDAO
import com.example.bimu.data.models.AuxClass
import com.example.bimu.data.models.User
import com.example.bimu.data.network.ApiClient
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var userDao: UserDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userDao = UserDAO(ApiClient.userApi)

        findViewById<Button>(R.id.registerBtn).setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            val username = findViewById<EditText>(R.id.nameEditText).text.toString()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Todos los campos deben rellenarse", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val user = try {
                        userDao.registerUser(
                            User(
                                username = username,
                                email = email,
                                password = password // <-- asegúrate de que está el campo password en el modelo y API
                            )
                        )
                    } catch (e: Exception) {
                        null
                    }

                    if (user != null && user._id != null) {
                        AuxClass().saveUserIdToPrefs(this@RegisterActivity, user._id)
                        Toast.makeText(this@RegisterActivity, "Usuario registrado: ${user.username}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                        intent.putExtra("fromRegister", true)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findViewById<Button>(R.id.loginBtnReg).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}