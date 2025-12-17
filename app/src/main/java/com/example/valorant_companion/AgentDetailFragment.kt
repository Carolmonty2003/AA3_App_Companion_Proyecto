package com.example.valorant_companion

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import ValorantApi.ValorantApiInstance
import ValorantApi.ValorantAgentDetailResponse
import com.example.valorant_companion.utils.SimpleImageLoader
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AgentDetailFragment : Fragment(R.layout.fragment_agent_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uuid = requireArguments().getString(ARG_UUID, "")

        val loading = view.findViewById<View>(R.id.agent_detail_loading_splash)
        val portrait = view.findViewById<ImageView>(R.id.agent_portrait)
        val nameTv = view.findViewById<TextView>(R.id.agent_name)
        val roleTv = view.findViewById<TextView>(R.id.agent_role)
        val descTv = view.findViewById<TextView>(R.id.agent_desc)
        val abilitiesContainer = view.findViewById<LinearLayout>(R.id.agent_abilities_container)

        loading.visibility = View.VISIBLE

        ValorantApiInstance.api.getAgentByUuid(uuid, "es-ES")
            .enqueue(object : Callback<ValorantAgentDetailResponse> {

                override fun onResponse(
                    call: Call<ValorantAgentDetailResponse>,
                    response: Response<ValorantAgentDetailResponse>
                ) {
                    loading.visibility = View.GONE
                    if (!response.isSuccessful) return

                    val agent = response.body()?.data ?: return

                    nameTv.text = agent.displayName ?: "Unknown"
                    roleTv.text = agent.role?.displayName ?: ""
                    descTv.text = agent.description ?: ""

                    SimpleImageLoader.load(
                        agent.fullPortraitV2,
                        portrait,
                        fallbackResId = R.drawable.ic_launcher_foreground
                    )

                    // Habilites q se añaden dinamicamente
                    abilitiesContainer.removeAllViews()
                    val abilities = agent.abilities ?: emptyList()

                    for (ab in abilities) {
                        val row = layoutInflater.inflate(R.layout.item_agent_hability, abilitiesContainer, false)

                        val icon = row.findViewById<ImageView>(R.id.ability_icon)
                        val title = row.findViewById<TextView>(R.id.ability_name)
                        val desc = row.findViewById<TextView>(R.id.ability_desc)

                        title.text = ab.displayName ?: ""
                        desc.text = ab.description ?: ""

                        SimpleImageLoader.load(
                            ab.displayIcon,
                            icon,
                            fallbackResId = R.drawable.ic_launcher_foreground
                        )

                        abilitiesContainer.addView(row)
                    }
                }

                override fun onFailure(call: Call<ValorantAgentDetailResponse>, t: Throwable) {
                    loading.visibility = View.GONE
                }
            })
    }

    companion object {
        private const val ARG_UUID = "uuid"

        fun newInstance(uuid: String) = AgentDetailFragment().apply {
            arguments = Bundle().apply { putString(ARG_UUID, uuid) }
        }
    }
}
