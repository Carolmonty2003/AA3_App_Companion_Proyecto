package com.example.valorant_companion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/*
 * Adapter del RecyclerView para el chat global.
 * Se encarga de:
 * - Pintar una lista de mensajes (ChatMessage)
 * - Diferenciar entre mensajes "míos" y "de otros" para usar layouts distintos
 *
 * (IA): uso de 2 tipos de item (TYPE_ME / TYPE_OTHER) con getItemViewType()
 * para inflar layouts diferentes dependiendo del UID.
 */
class ChatAdapter(
    private val messages: MutableList<ChatMessage>,
    private val myUid: String,
    private var myAvatarIdLive: String = "default",
    private var myNameLive: String = "Player"
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    /*
     * Constantes internas para distinguir el tipo de fila:
     * - TYPE_ME: mensaje enviado por el usuario actual
     * - TYPE_OTHER: mensaje enviado por otra persona
     *
     */
    companion object {
        private const val TYPE_ME = 1
        private const val TYPE_OTHER = 2
    }

    /*
     * Decide qué tipo de fila toca en cada posición del RecyclerView.
     * Compara el uid del mensaje con myUid (uid del usuario logueado).
     *
     * @param position Posición del item en la lista.
     * @return TYPE_ME si el mensaje es del usuario actual, TYPE_OTHER si es de otro.
     *
     * (IA): esta técnica de "multiple view types" es el patrón estándar para chats (2 layouts distintos).
     */
    override fun getItemViewType(position: Int): Int {
        val msgUid = messages[position].uid
        return if (!msgUid.isNullOrBlank() && msgUid == myUid) TYPE_ME else TYPE_OTHER
    }

    /*
     * ViewHolder para mensajes propios.
     * Guarda referencias a las views del layout item_message_me.
     *
     */
    class MeVH(v: View) : RecyclerView.ViewHolder(v) {
        val avatar: ImageView = v.findViewById(R.id.avatar)
        val user: TextView = v.findViewById(R.id.msg_user)
        val text: TextView = v.findViewById(R.id.msg_text)
    }

    /*
     * ViewHolder para mensajes de otras personas.
     * Guarda referencias a las views del layout item_message_other.
     *
     * Nota: es igual que el MeVH, pero se separa porque cada uno infla un layout distinto.
     */
    class OtherVH(v: View) : RecyclerView.ViewHolder(v) {
        val avatar: ImageView = v.findViewById(R.id.avatar)
        val user: TextView = v.findViewById(R.id.msg_user)
        val text: TextView = v.findViewById(R.id.msg_text)
    }

    /*
     * Crea el ViewHolder correspondiente según el viewType.
     * - Si TYPE_ME -> infla item_message_me
     * - Si TYPE_OTHER -> infla item_message_other
     *
     * @param parent Contenedor donde se insertará el item.
     * @param viewType Tipo devuelto por getItemViewType().
     * @return ViewHolder listo para usar/reutilizar.
     *
     * (IA): inflar distintos layouts según viewType (patrón de chats).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ME) {
            MeVH(inf.inflate(R.layout.item_message_me, parent, false))
        } else {
            OtherVH(inf.inflate(R.layout.item_message_other, parent, false))
        }
    }

    /*
     * Asigna los datos del mensaje a las views del ViewHolder.
     * - Si name viene null, usa "Player" como fallback.
     * - Si text viene null, usa "" para evitar crash.
     *
     * @param holder ViewHolder actual (MeVH u OtherVH).
     * @param position Posición del mensaje en la lista.
     *
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val m = messages[position]
        val name = m.name ?: "Player"
        val text = m.text ?: ""

        // si mensaje es mío, usar el avatar ACTUAL (aunque el mensaje sea antiguo).
        val avatarIdToUse = if (!m.uid.isNullOrBlank() && m.uid == myUid) {
            myAvatarIdLive
        } else {
            m.avatarId
        }

        // si el mensaje es mío, usar el nombre ACTUAL
        val nameToUse = if (!m.uid.isNullOrBlank() && m.uid == myUid) {
            myNameLive
        } else {
            m.name ?: "Player"
        }


        val avatarRes = when (avatarIdToUse) {
            "omen" -> R.drawable.avatar_omen
            "deadlock" -> R.drawable.avatar_deadlock
            "viper" -> R.drawable.avatar_viper
            "clove" -> R.drawable.avatar_clove
            "default" -> R.drawable.avatar_sage
            null -> R.drawable.avatar_sage
            else -> R.drawable.avatar_sage
        }

        when (holder) {
            is MeVH -> {
                holder.user.text = nameToUse
                holder.text.text = text
                holder.avatar.setImageResource(avatarRes)
            }
            is OtherVH -> {
                holder.user.text = nameToUse
                holder.text.text = text
                holder.avatar.setImageResource(avatarRes)
            }
        }
    }

    /*
     * Devuelve cuántos mensajes hay.
     *
     * @return Número total de items del RecyclerView.
     */
    override fun getItemCount() = messages.size

    /*
     * Añade un mensaje al final y notifica SOLO el nuevo item.
     * Esto es mejor que notifyDataSetChanged() porque evita redibujar toda la lista.
     *
     * @param msg Mensaje recibido/enviado a añadir.
     *
     * (IA): notifyItemInserted(...) es un detalle de rendimiento/UX;
     * en apuntes a veces se usa notifyDataSetChanged() por simplicidad.
     */
    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }

    // actualizar avatar actual del usuario y refrescar lista
    fun updateMyAvatar(newAvatarId: String) {
        myAvatarIdLive = newAvatarId
        notifyDataSetChanged()
    }

    //actualizar nombre actual del usuario y refrescar lista
    fun updateMyName(newName: String) {
        myNameLive = newName
        notifyDataSetChanged()
    }

}
