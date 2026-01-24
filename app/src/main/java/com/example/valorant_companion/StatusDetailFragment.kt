package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class StatusDetailFragment : Fragment(R.layout.fragment_status_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ocultamos splash de carga (si lo incluyes como en tu detail)
        val loading = view.findViewById<View>(R.id.detail_loading_splash)
        loading.visibility = View.GONE

        val title = requireArguments().getString(ARG_TITLE, "Status")
        val type = requireArguments().getString(ARG_TYPE, "")
        val state = requireArguments().getString(ARG_STATE, "")
        val createdAt = requireArguments().getString(ARG_DATE, "")
        val content = requireArguments().getString(ARG_CONTENT, "")

        view.findViewById<TextView>(R.id.status_detail_title).text = title
        view.findViewById<TextView>(R.id.status_detail_meta).text = "$type • $state"
        view.findViewById<TextView>(R.id.status_detail_date).text = createdAt
        view.findViewById<TextView>(R.id.status_detail_content).text = content
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_TYPE = "type"
        private const val ARG_STATE = "state"
        private const val ARG_DATE = "date"
        private const val ARG_CONTENT = "content"

        fun newInstance(
            title: String,
            type: String,
            state: String,
            createdAt: String,
            content: String
        ) = StatusDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_TYPE, type)
                putString(ARG_STATE, state)
                putString(ARG_DATE, createdAt)
                putString(ARG_CONTENT, content)
            }
        }
    }
}
