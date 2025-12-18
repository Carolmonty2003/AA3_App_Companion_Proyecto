package ValorantApi

data class ValorantMapsResponse(
    val status: Int,
    val data: List<ValorantMap>
)

data class ValorantMap(
    val uuid: String?,
    val displayName: String?,     // <- nombre del mapa
    val listViewIcon: String?,    // <- imagen de la lista
    val splash: String?,          // <- fondo del detalle
    val displayIcon: String?      // <- imagen del minimapa grande en detalle
)

data class ValorantAgentsResponse(
    val status: Int,
    val data: List<ValorantAgent>
)

data class ValorantAgentDetailResponse(
    val status: Int,
    val data: ValorantAgent
)

data class ValorantAgent(
    val uuid: String?,
    val displayName: String?,
    val description: String?,
    val displayIcon: String?,     // icono cuadrado (lista)
    val fullPortraitV2: String?,  // imagen grande para detalle
    val role: ValorantRole?,
    val abilities: List<ValorantAbility>?
)

data class ValorantRole(
    val displayName: String?,
    val displayIcon: String?
)

data class ValorantAbility(
    val slot: String?,          // Q/E/C/X/Pasiva
    val displayName: String?,
    val description: String?,
    val displayIcon: String?
)