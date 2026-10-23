package com.creativeam.widvelo.incident

data class RoadIncidentAlert(
    val entity: RoadIncidentEntity,
    val distanceMeters: Int,
    val isAhead: Boolean
) {
    val formattedDistance: String
        get() = if (distanceMeters >= 1000) {
            String.format("%.1f km", distanceMeters / 1000.0)
        } else {
            "${distanceMeters}m"
        }

    val displaySummary: String
        get() = "${entity.type.badge} ${entity.type.label} (${formattedDistance}) - ${entity.roadName}"
}
