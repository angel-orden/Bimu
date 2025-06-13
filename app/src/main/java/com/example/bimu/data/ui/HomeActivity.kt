package com.example.bimu.data.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bimu.R
import com.example.bimu.data.ui.fragments.ChatsFragment
import com.example.bimu.data.ui.fragments.ProfileFragment
import com.example.bimu.data.ui.fragments.RouteListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        //mostrar rutas por defecto
        if (savedInstanceState == null) {
            val fromRegister = intent.getBooleanExtra("fromRegister", false)
            if (fromRegister) {
                //Si viene desde el registro, se le llevará al perfil para completarlo
                replaceFragment(ProfileFragment())
            } else {
                replaceFragment(RouteListFragment())
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_rutas -> replaceFragment(RouteListFragment())
                R.id.nav_chat -> replaceFragment(ChatsFragment())
                R.id.nav_perfil -> replaceFragment(ProfileFragment())
                else -> false
            }
            }
    }

    private fun replaceFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }
}