package com.creativeam.widvelo.trafficlight

/**
 * Alerta ativo de semáforo próximo e estado do temporizador de ciclo.
 */
data class TrafficLightAlert(
    val entity: TrafficLightEntity,
    val distanceMeters: Int,
    val isAtStandstill: Boolean,
    val elapsedWaitingSeconds: Int = 0,
    val estimatedRemainingSeconds: Int = 0,
    val progressFraction: Float = 0f
) {
    val title: String = "${entity.roadName} x ${entity.crossRoadName}"

    val formattedDistance: String
        get() = when {
            distanceMeters < 30 && isAtStandstill -> "No Semáforo"
            distanceMeters < 1000 -> "${distanceMeters}m"
            else -> String.format("%.1f km", distanceMeters / 1000.0)
        }

    val statusText: String
        get() = when {
            isAtStandstill && estimatedRemainingSeconds > 0 -> "Aguarde ~${estimatedRemainingSeconds}s"
            isAtStandstill -> "Parado no Farol"
            entity.hasRedLightCamera -> "Semáforo com Câmera (${formattedDistance})"
            else -> "Semáforo (${formattedDistance})"
        }
}
