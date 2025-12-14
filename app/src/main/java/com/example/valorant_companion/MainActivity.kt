package com.example.valorant_companion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.top_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)

        // Overflow icon (3 rayas)
        toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu)
        toolbar.overflowIcon?.setTint(ContextCompat.getColor(this, R.color.white))

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

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_maps
            select(R.id.nav_maps)
        }

        bottomNav.setOnItemSelectedListener { item ->
            select(item.itemId)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val tx = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val name = intent.getStringExtra("USER_NAME")
                val image = intent.getStringExtra("USER_IMAGE")

                findViewById<TextView>(R.id.toolbar_title).text = "Profile"
                replaceFragment(ProfileFragment.newInstance(name, image), addToBackStack = true)
                true
            }
            R.id.settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
