package com.example.valorant_companion

data class ChatMessage(
    var uid: String? = null,
    var name: String? = null,
    var text: String? = null,
    var timestamp: Long? = null
) {
    constructor() : this(null, null, null, null)
}
