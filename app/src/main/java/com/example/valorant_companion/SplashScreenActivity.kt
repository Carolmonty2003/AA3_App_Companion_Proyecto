package com.example.valorant_companion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/*
 * Activity de Splash Screen.
 * Su único objetivo es mostrar el layout de splash mientras arranca la app.
 *
 * Dependiendo de los apuntes, la splash puede hacerse con:
 * - Activity propia (como aquí), o
 * - SplashScreen API / tema de inicio.
 * Esta versión es la más sencilla y válida para clase.
 */
class SplashScreenActivity : AppCompatActivity() {

    /**
     * Punto de entrada de la Activity.
     * Carga el layout `activity_splashscreen`.
     *
     * @param {Bundle?} savedInstanceState - Estado guardado si se recrea la Activity (rotación, etc.).
     * @returns {Unit} No devuelve nada.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)
    }
}
