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

    /*
     * Inicializa la pantalla principal con:
     * - Toolbar fija (sin título nativo, usando un TextView propio).
     * - BottomNavigation para cambiar entre fragments principales.
     * - Control del BackStack para:
     *   - Mostrar/ocultar flecha de volver.
     *   - Actualizar el título según el fragment actual.
     *
     * (IA): limpiar el backStack al cambiar de tab, y el listener de backStack para actualizar UI
     * suele ser una “mejora práctica”, no siempre viene literal en apuntes.
     *
     * @param {Bundle?} savedInstanceState - Estado guardado (rotación, recreación).
     * @returns {Unit}
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.top_toolbar)
        setSupportActionBar(toolbar)

        // Usas un TextView como título en vez del título nativo de la ActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)

        /*
         * Configura el icono del overflow (las “3 rayas”) y lo tiñe en blanco.
         */
        toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu)
        toolbar.overflowIcon?.setTint(ContextCompat.getColor(this, R.color.white))

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        /*
         * Función local que decide qué Fragment cargar según el tab pulsado.
         *
         * @param {Int} tabId - id del item pulsado en el menú del BottomNav.
         * @returns {Unit}
         */
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

        // Primer fragment al abrir la app
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_maps
            select(R.id.nav_maps)
        }

        /*
         * Listener del BottomNavigation:
         * - Limpia pantallas “secundarias” (Profile/Settings/Detail) cuando cambias de tab.
         * - Luego carga el fragment principal.
         *
         * (IA): popBackStack pero es una solución muy común para que el usuario no vuelva a
         * pantallas “internas” de otro apartado al cambiar de sección.
         */
        bottomNav.setOnItemSelectedListener { item ->
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            select(item.itemId)
            true
        }

        // La flecha de volver (navigationIcon) se comporta como un “back”.
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        /*
         * Cada vez que cambie el backStack:
         * - se muestra/oculta la flecha
         * - se actualiza el título según el fragment visible
         *
         * Este listener es una forma de mantener toolbar sincronizada.
         */
        supportFragmentManager.addOnBackStackChangedListener {
            updateToolbarBackIcon(toolbar)
            updateToolbarTitleFromCurrentFragment()
        }

        // Primera actualización manual al arrancar
        updateToolbarBackIcon(toolbar)
        updateToolbarTitleFromCurrentFragment()
    }

    /*
     * Reemplaza el fragment actual por otro dentro del contenedor.
     * Si addToBackStack = true, permite volver atrás con la flecha/back.
     *
     * @param {Fragment} fragment - Fragment destino que quieres mostrar.
     * @param {Boolean} addToBackStack - Si quieres poder volver atrás.
     * @returns {Unit}
     */
    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val tx = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }

    /*
     * Infla el menú del toolbar (Profile / Settings).
     *
     * @param {Menu} menu - Menú a inflar.
     * @returns {Boolean} true si se muestra.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    /*
     * Gestiona clicks del menú del toolbar (overflow):
     * - Profile -> abre ProfileFragment y lo mete en backStack
     * - Settings -> abre SettingsFragment y lo mete en backStack
     *
     * @param {MenuItem} item - Elemento pulsado del menú.
     * @returns {Boolean} true si se consume el evento.
     */
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
                findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.SettingsTitle)
                replaceFragment(SettingsFragment(), addToBackStack = true)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /*
     * Muestra/oculta la flecha de “volver” en la toolbar según el backStack.
     *
     * - Si hay pantallas en backStack -> pone navigationIcon (ic_back) y lo tiñe en blanco.
     * - Si no hay nada -> quita el icono.
     *
     * (IA): aquí estás usando ScaleDrawable para “escalar” el icono.
     *
     * @param {MaterialToolbar} toolbar - Toolbar donde quieres poner/quitar el back icon.
     * @returns {Unit}
     */
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

    /*
     * Actualiza el título del toolbar según el fragment que esté visible.
     *
     * - Para MapDetailFragment intenta mostrar el nombre del mapa (displayName en arguments).
     *
     * @returns {Unit}
     */
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
