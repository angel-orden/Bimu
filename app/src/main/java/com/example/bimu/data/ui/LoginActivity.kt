package com.example.bimu.data.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bimu.R
import com.example.bimu.data.dao.UserDAO
import com.example.bimu.data.models.AuxClass
import com.example.bimu.data.models.User
import com.example.bimu.data.network.ApiClient // <-- Asegúrate de importar esto
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var userDao: UserDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userDao = UserDAO(ApiClient.userApi)

        val loginButton = findViewById<Button>(R.id.loginBtn)
        val progressBar = findViewById<ProgressBar>(R.id.loginProgressBar)

        loginButton.setOnClickListener {
            loginButton.isEnabled = false
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Todos los campos deben rellenarse", Toast.LENGTH_SHORT).show()
                loginButton.isEnabled = true
            } else {
                progressBar.visibility = ProgressBar.VISIBLE
                lifecycleScope.launch {
                    try {
                        val user = userDao.login(email, password)
                        if (user != null && user._id != null) {
                            AuxClass().saveUserIdToPrefs(this@LoginActivity, user._id)
                            Toast.makeText(this@LoginActivity, "Login exitoso: ${user.username}", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Error: ${e.localizedMessage ?: "Error desconocido"}", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", "Login error", e)
                    }
                    progressBar.visibility = ProgressBar.GONE
                    loginButton.isEnabled = true
                }
            }
        }

        findViewById<Button>(R.id.registerBtnLog).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}