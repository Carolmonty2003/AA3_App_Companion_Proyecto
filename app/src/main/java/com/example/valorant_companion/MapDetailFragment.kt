package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.valorant_companion.utils.SimpleImageLoader

/*
 * Fragment de detalle de un mapa.
 * Muestra:
 * - splash (imagen de fondo)
 * - displayIcon (imagen principal del mapa)
 * - displayName (título)
 *
 */
class MapDetailFragment : Fragment(R.layout.fragment_map_detail) {

    /*
     * Se ejecuta cuando el layout ya está inflado y puedes usar findViewById.
     *
     * Flujo:
     * 1) Recupera argumentos (name, displayIcon, splash)
     * 2) Muestra una splash/loading encima
     * 3) Carga 2 imágenes asíncronas (splash + icon)
     * 4) Cuando ambas terminan -> oculta loading
     *
     * (IA) El contador "pending" + callback doneOne() para esperar a 2 cargas.
     *
     * @param {View} view - Vista raíz del fragment ya inflada.
     * @param {Bundle?} savedInstanceState - Estado guardado (si existe).
     * @returns {Unit} No devuelve nada.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperas datos pasados desde el fragment anterior (MapsFragment)
        val name = requireArguments().getString(ARG_NAME, "")
        val displayIcon = requireArguments().getString(ARG_DISPLAY_ICON, "")
        val splash = requireArguments().getString(ARG_SPLASH, "")

        // Pantalla de carga (splash screen incluida como overlay en el XML)
        val loading = view.findViewById<View>(R.id.detail_loading_splash)
        loading.visibility = View.VISIBLE

        val splashIv = view.findViewById<ImageView>(R.id.map_splash)
        val iconIv = view.findViewById<ImageView>(R.id.map_display_icon)

        // Título del mapa
        view.findViewById<TextView>(R.id.map_name).text = name

        /*
         * (IA)
         * Como cargas 2 imágenes por separado, necesitas saber cuándo han terminado ambas.
         * pending=2 -> cuando cada imagen termina llama a doneOne() -> al llegar a 0 ocultas loading.
         */
        var pending = 2
        fun doneOne() {
            pending--
            if (pending <= 0) loading.visibility = View.GONE
        }

        /*
         * Carga imágenes desde URL:
         * - splash -> fondo
         * - displayIcon -> imagen centrada del mapa
         *
         * (IA): SimpleImageLoader es una alternativa a Glide/Picasso.
         * En apuntes normalmente se usaría una librería, pero aquí se usa un loader “casero”.
         */
        SimpleImageLoader.load(splash, splashIv, R.drawable.ic_launcher_foreground) { doneOne() }
        SimpleImageLoader.load(displayIcon, iconIv, R.drawable.ic_launcher_foreground) { doneOne() }
    }

    companion object {
        //Keys para el Bundle de argumentos.
        private const val ARG_NAME = "name"
        private const val ARG_DISPLAY_ICON = "displayIcon"
        private const val ARG_SPLASH = "splash"

        /*
         * Creador "newInstance" para construir el fragment con argumentos.
         * Esto evita depender de setters o variables globales.
         *
         * @param {String} displayName - Nombre del mapa.
         * @param {String} displayIcon - URL imagen del mapa (principal).
         * @param {String} splash - URL imagen splash/fondo.
         * @returns {MapDetailFragment} Fragment listo para usarse con arguments.
         */
        fun newInstance(displayName: String, displayIcon: String, splash: String) =
            MapDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, displayName)
                    putString(ARG_DISPLAY_ICON, displayIcon)
                    putString(ARG_SPLASH, splash)
                }
            }
    }
}
