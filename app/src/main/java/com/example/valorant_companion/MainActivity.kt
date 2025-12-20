package com.example.valorant_companion

import android.graphics.drawable.ScaleDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager

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
                    toolbarTitle.setText(R.string.MapsTitle)
                    replaceFragment(MapsFragment())
                }
                R.id.nav_agents -> {
                    toolbarTitle.setText(R.string.AgentsTitle)
                    replaceFragment(AgentsFragment())
                }
                R.id.nav_events -> {
                    toolbarTitle.setText(R.string.EventsTitle)
                    replaceFragment(EventsFragment())
                }
                R.id.nav_chat -> {
                    toolbarTitle.setText(R.string.ChatTitle)
                    val name = intent.getStringExtra("USER_NAME")
                    replaceFragment(ChatFragment.newInstance(name))
                }

            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_maps
            select(R.id.nav_maps)
        }

        bottomNav.setOnItemSelectedListener { item ->
            // Limpia pantallas "extra" (Profile/Settings/MapDetail) al cambiar de tab
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

            select(item.itemId)
            true
        }

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Mostrar/ocultar el back según backstack
        supportFragmentManager.addOnBackStackChangedListener {
            updateToolbarBackIcon(toolbar)
            updateToolbarTitleFromCurrentFragment()
        }

        // Llamada inicial
        updateToolbarBackIcon(toolbar)
        updateToolbarTitleFromCurrentFragment()
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

                findViewById<TextView>(R.id.toolbar_title).setText(R.string.ProfileTitle)
                replaceFragment(ProfileFragment.newInstance(name, image), addToBackStack = true)
                true
            }
            R.id.settings -> {
                // Cambia título del toolbar (si lo gestionas con TextView)
                findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.SettingsTitle)

                replaceFragment(SettingsFragment(), addToBackStack = true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateToolbarBackIcon(toolbar: MaterialToolbar) {
        val canGoBack = supportFragmentManager.backStackEntryCount > 0

        if (canGoBack) {
            val d = AppCompatResources.getDrawable(this, R.drawable.ic_back) ?: return
            d.setTint(ContextCompat.getColor(this, R.color.white))

            val scaled = ScaleDrawable(d, Gravity.CENTER, 2f, 2f).apply {
                level = 10000
            }

            toolbar.navigationIcon = scaled
        } else {
            toolbar.navigationIcon = null
        }
    }



    private fun updateToolbarTitleFromCurrentFragment() {
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        val current = supportFragmentManager.findFragmentById(R.id.fragment_container)

        toolbarTitle.text = when (current) {
            is MapsFragment -> "Maps"
            is AgentsFragment -> "Agents"
            is EventsFragment -> "Events"
            is ChatFragment -> "Chat"
            is ProfileFragment -> "Profile"
            is SettingsFragment -> "Settings"
            is MapDetailFragment -> current.arguments?.getString("displayName") ?: "Maps"
            else -> "Maps"
        }
    }

}
