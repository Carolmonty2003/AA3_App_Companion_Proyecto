package com.example.valorant_companion

/*
 * Modelo de datos (data class) que representa un mensaje del chat.
 * Se usa para:
 * - Guardar en Firebase Realtime Database (setValue)
 * - Leer desde Firebase (snapshot.getValue(ChatMessage::class.java))
 *
 */
data class ChatMessage(
    var uid: String? = null,
    val avatarId: String? = null,
    var name: String? = null,
    var text: String? = null,
    var timestamp: Long? = null
) {
    /*
     * Constructor vacío necesario para Firebase.
     * Firebase Realtime Database necesita poder crear el objeto sin parámetros
     * para luego rellenar los campos (reflection).
     *
     * (IA): Evita problemas en algunos casos y es compatible 100%.
     */
    constructor() : this(null, null, null, null, null)
}
