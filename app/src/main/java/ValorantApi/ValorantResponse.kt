package ValorantApi

/**
 * Respuesta del endpoint de MAPS.
 * - status: código de estado lógico de la API
 * - data: lista de mapas
 */
data class ValorantMapsResponse(
    val status: Int,
    val data: List<ValorantMap>
)

/**
 * Modelo de un mapa.
 * Campos principales que usas en UI:
 * - displayName: nombre del mapa
 * - listViewIcon: imagen del item en la lista (RecyclerView)
 * - splash: imagen fondo del detalle
 * - displayIcon: imagen grande del mapa dentro del detalle
 */
data class ValorantMap(
    val uuid: String?,
    val displayName: String?,
    val listViewIcon: String?,
    val splash: String?,
    val displayIcon: String?
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