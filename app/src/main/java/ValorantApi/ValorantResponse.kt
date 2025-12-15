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