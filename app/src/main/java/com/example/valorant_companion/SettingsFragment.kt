package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

/*
 * Fragment de Settings:
 * - Permite cambiar idioma (es/en/ca) usando AppCompatDelegate.setApplicationLocales(...)
 * - Permite activar/desactivar modo nocturno guardando preferencia en SharedPreferences
 * - Reinicia la Activity para que los cambios se apliquen inmediatamente (idioma + night mode)
 *
 * Extra (IA ):
 * - Reinicio “fuerte” (finish + startActivity(intent)) para aplicar cambios al instante
 * - Uso exacto de AppCompatDelegate.getApplicationLocales()/setApplicationLocales()
 */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {
        /*
         * Constantes para SharedPreferences:
         * - PREFS: nombre del fichero de preferencias de la app
         * - KEY_NIGHT: clave donde guardas el estado del modo nocturno
         *
         */
        private const val PREFS = "valorant_prefs"
        private const val KEY_NIGHT = "night_mode"
    }

    /*
     * Inicializa la UI del fragment y conecta los listeners:
     * 1) Referencias de views (RadioGroup, Button, CheckBox)
     * 2) NIGHT MODE:
     *    - Lee preferencia guardada
     *    - Al cambiar, guarda y aplica AppCompatDelegate.setDefaultNightMode(...)
     *    - Reinicia la Activity para refrescar toda la UI
     * 3) IDIOMA:
     *    - Detecta idioma actual
     *    - Marca el radio correspondiente
     *    - Al aplicar, setApplicationLocales(...) y reinicia Activity
     *
     * @param {View} view - Vista raíz inflada del fragment.
     * @param {Bundle?} savedInstanceState - Estado guardado si existe.
     * @returns {Unit} No devuelve nada.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val group = view.findViewById<RadioGroup>(R.id.lang_group)
        val apply = view.findViewById<Button>(R.id.btn_apply_lang)
        val night = view.findViewById<CheckBox>(R.id.cb_night_mode)

        // --- NIGHT MODE ---
        /*
         * Lee preferencias para recordar el estado del modo nocturno.
         * Si el usuario sale y vuelve a entrar, el checkbox queda como lo dejó.
         */
        val prefs = requireContext().getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        night.isChecked = prefs.getBoolean(KEY_NIGHT, false)

        /*
         * Listener del checkbox:
         * - Guarda el valor en preferencias
         * - Aplica el modo noche con AppCompatDelegate.setDefaultNightMode(...)
         * - Reinicia la Activity para que el tema se aplique a todo inmediatamente
         *
         * (IA ):
         * - Reinicio “fuerte” con finish() y startActivity(intent).
         */
        night.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_NIGHT, isChecked).apply()

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            // Reinicio “fuerte” (igual que haces con el idioma)
            val act = requireActivity()
            act.finish()
            act.startActivity(act.intent)
        }

        // --- IDIOMA  ---
        /*
         * Obtiene el idioma actual de la app (como tags tipo "es", "en", "ca"...).
         * Se usa para marcar el RadioButton correcto al entrar en settings.
         *
         * Nota: esto depende de AppCompat (AndroidX).
         * Puede estar o no en apuntes según lo que haya explicado el profe.
         */
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()

        when {
            currentTags.startsWith("es") -> group.check(R.id.lang_es)
            currentTags.startsWith("en") -> group.check(R.id.lang_en)
            currentTags.startsWith("ca") -> group.check(R.id.lang_cat)
            else -> group.check(R.id.lang_en)
        }

        /*
         * Al pulsar “Apply”:
         * - Traduce el radio seleccionado a un tag ("es", "en", "ca")
         * - Construye LocaleListCompat
         * - Aplica AppCompatDelegate.setApplicationLocales(...)
         * - Reinicia Activity para refrescar textos y recursos
         *
         * (IA):
         * - Uso de LocaleListCompat.getEmptyLocaleList() para limpiar.
         */
        apply.setOnClickListener {
            val tag = when (group.checkedRadioButtonId) {
                R.id.lang_es -> "es"
                R.id.lang_en -> "en"
                R.id.lang_cat -> "ca"
                else -> ""
            }

            val locales = if (tag.isBlank()) {
                androidx.core.os.LocaleListCompat.getEmptyLocaleList()
            } else {
                androidx.core.os.LocaleListCompat.forLanguageTags(tag)
            }

            AppCompatDelegate.setApplicationLocales(locales)

            val act = requireActivity()
            act.finish()
            act.startActivity(act.intent)
        }
    }
}
