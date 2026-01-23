// ValStatusModels.kt
package RiotStatusApi

data class ValPlatformStatusResponse(
    val maintenances: List<ValStatusItem>? = null,
    val incidents: List<ValStatusItem>? = null
)

data class ValStatusItem(
    val id: String? = null,
    val maintenance_status: String? = null,
    val incident_severity: String? = null,
    val titles: List<ValLocalizedText>? = null,
    val updates: List<ValStatusUpdate>? = null,
    val created_at: String? = null
)

data class ValStatusUpdate(
    val translations: List<ValLocalizedText>? = null
)

data class ValLocalizedText(
    val locale: String? = null,
    val title: String? = null,
    val content: String? = null
)
