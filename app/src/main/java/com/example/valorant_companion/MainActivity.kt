package com.example.valorant_companion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.top_toolbar)
        setSupportActionBar(toolbar)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        fun select(tabId: Int) {
            when (tabId) {
                R.id.nav_maps -> {
                    toolbar.title = "Maps"
                    replaceFragment(MapsFragment())
                }
                R.id.nav_agents -> {
                    toolbar.title = "Agents"
                    //replaceFragment(AgentsFragment())
                }
                R.id.nav_events -> {
                    toolbar.title = "Events"
                    //replaceFragment(EventsFragment())
                }
                R.id.nav_chat -> {
                    toolbar.title = "Chat"
                    //replaceFragment(ChatFragment())
                }
            }
        }

        // Fragment por defecto
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_maps
            select(R.id.nav_maps)
        }

        bottomNav.setOnItemSelectedListener { item ->
            select(item.itemId)
            true
        }

        toolbar.setNavigationOnClickListener {
            // lo q se abra
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
