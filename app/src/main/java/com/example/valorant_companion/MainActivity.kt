package com.example.valorant_companion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Fragment por defecto
        if (savedInstanceState == null) {
            //replaceFragment(MapsFragment())
            bottomNav.selectedItemId = R.id.nav_maps
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                //R.id.nav_maps -> replaceFragment(MapsFragment())
                //R.id.nav_agents -> replaceFragment(AgentsFragment())
                //R.id.nav_events -> replaceFragment(EventsFragment())
                //R.id.nav_chat -> replaceFragment(ChatFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
