package com.example.valorant_companion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.top_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        fun select(tabId: Int) {
            when (tabId) {
                R.id.nav_maps -> {
                    toolbarTitle.text = "Maps"
                    replaceFragment(MapsFragment())
                }
                R.id.nav_agents -> {
                    toolbarTitle.text = "Agents"
                    replaceFragment(AgentsFragment())
                }
                R.id.nav_events -> {
                    toolbarTitle.text = "Events"
                    replaceFragment(EventsFragment())
                }
                R.id.nav_chat -> {
                    toolbarTitle.text = "Chat"
                    replaceFragment(ChatFragment())
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
