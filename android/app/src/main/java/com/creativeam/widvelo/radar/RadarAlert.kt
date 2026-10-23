package com.creativeam.widvelo.radar

/**
 * Representa um alerta ativo de radar próximo em rota de colisão geográfica com o veículo.
 */
data class RadarAlert(
    val radar: RadarEntity,
    val distanceMeters: Int,
    val isDirectionalMatch: Boolean,
    val isImminent: Boolean, // <= 100 metros
    val alertStage: AlertStage
) {
    val formattedDistance: String
        get() = if (distanceMeters < 1000) {
            "${distanceMeters} m"
        } else {
            String.format(java.util.Locale.US, "%.1f km", distanceMeters / 1000.0)
        }
}

enum class AlertStage {
    FAR,       // Fora da faixa de alerta (> 500m)
    APPROACH,  // Aproximação (300m - 500m)
    IMMINENT   // Ponto Iminente (<= 100m)
}
