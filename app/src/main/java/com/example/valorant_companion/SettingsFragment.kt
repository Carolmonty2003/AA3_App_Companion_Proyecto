package com.example.valorant_companion.com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.example.valorant_companion.R

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val group = view.findViewById<RadioGroup>(R.id.lang_group)
        val apply = view.findViewById<Button>(R.id.btn_apply_lang)

        // Idioma actual guardado por AppCompat (si está vacío => “Sistema”)
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

            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(locales)

            // Reinicio “fuerte” (más fiable que recreate a veces)
            val act = requireActivity()
            act.finish()
            act.startActivity(act.intent)
        }

    }
}
