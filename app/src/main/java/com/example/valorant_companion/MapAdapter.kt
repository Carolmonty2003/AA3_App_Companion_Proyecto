package com.example.valorant_companion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ValorantApi.ValorantMap
import com.example.valorant_companion.utils.SimpleImageLoader

/*
 * Adapter del RecyclerView para mostrar la lista de mapas.
 * Se encarga de:
 * - Pintar cada item (item_map.xml)
 * - Asociar datos del modelo (ValorantMap) a la UI (imagen + nombre)
 * - Gestionar el click de cada mapa (para navegar al detalle)
 *
 * @param {List<ValorantMap>} items - Lista inicial de mapas.
 * @param {(ValorantMap) -> Unit} onClick - Callback que se ejecuta al pulsar un item.
 * @returns {Unit} No devuelve nada.
 *
 */
class MapAdapter(
    private var items: List<ValorantMap>,
    private val onClick: (ValorantMap) -> Unit
) : RecyclerView.Adapter<MapAdapter.MapVH>() {

    /*
     * Sustituye la lista del adapter por una nueva y repinta todo.
     *
     * @param {List<ValorantMap>} newItems - Nueva lista a mostrar.
     * @returns {Unit} No devuelve nada.
     *
     */
    fun submit(newItems: List<ValorantMap>) {
        items = newItems
        notifyDataSetChanged()
    }

    /*
     * Crea (infla) el layout de cada item del RecyclerView.
     * Aquí se convierte item_map.xml en una View real.
     *
     * @param {ViewGroup} parent - Contenedor donde se insertará el item.
     * @param {Int} viewType - Tipo de vista (no lo usas porque solo hay un tipo).
     * @returns {MapVH} ViewHolder creado.
     *
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_map, parent, false)
        return MapVH(v, onClick)
    }

    /*
     * Une los datos del mapa con la UI del ViewHolder (bind).
     *
     * @param {MapVH} holder - ViewHolder que se va a rellenar.
     * @param {Int} position - Posición del item en la lista.
     * @returns {Unit} No devuelve nada.
     */
    override fun onBindViewHolder(holder: MapVH, position: Int) {
        holder.bind(items[position])
    }

    /*
     * Devuelve cuántos elementos hay en la lista.
     *
     * @returns {Int} Tamaño actual de "items".
     */
    override fun getItemCount() = items.size

    /*
     * ViewHolder: guarda las referencias a las vistas de un item (ImageView y TextView)
     * para no estar haciendo findViewById todo el rato.
     *
     * @param {View} itemView - Vista inflada del item (item_map.xml).
     * @param {(ValorantMap) -> Unit} onClick - Acción al click.
     *
     */
    class MapVH(itemView: View, private val onClick: (ValorantMap) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val img: ImageView = itemView.findViewById(R.id.item_map_img)
        private val name: TextView = itemView.findViewById(R.id.item_map_name)

        /*
         * Rellena el item con la info del mapa.
         * - Muestra displayName
         * - Carga listViewIcon como imagen
         * - Configura el click para avisar al fragment/activity
         *
         * @param {ValorantMap} map - Mapa a pintar en el item.
         * @returns {Unit} No devuelve nada.
         */
        fun bind(map: ValorantMap) {
            name.text = map.displayName ?: "Unknown"

            //Carga la imagen del mapa (listViewIcon) en el ImageView.
            SimpleImageLoader.load(
                map.listViewIcon,
                img,
                fallbackResId = R.drawable.ic_launcher_foreground
            )

            // Cuando pulsas el item, se ejecuta el callback con el mapa seleccionado
            itemView.setOnClickListener { onClick(map) }
        }
    }
}
