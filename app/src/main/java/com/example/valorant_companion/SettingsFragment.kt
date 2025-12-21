package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {
        private const val PREFS = "valorant_prefs"
        private const val KEY_NIGHT = "night_mode"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val group = view.findViewById<RadioGroup>(R.id.lang_group)
        val apply = view.findViewById<Button>(R.id.btn_apply_lang)
        val night = view.findViewById<CheckBox>(R.id.cb_night_mode)

        // --- NIGHT MODE (como en apuntes, pero guardando preferencia) ---
        val prefs = requireContext().getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        night.isChecked = prefs.getBoolean(KEY_NIGHT, false)

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

        // --- IDIOMA (lo que ya tenías) ---
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()

        when {
            currentTags.startsWith("es") -> group.check(R.id.lang_es)
            currentTags.startsWith("en") -> group.check(R.id.lang_en)
            currentTags.startsWith("ca") -> group.check(R.id.lang_cat)
            else -> group.check(R.id.lang_en)
        }

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
