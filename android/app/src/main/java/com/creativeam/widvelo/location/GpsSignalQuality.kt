package com.creativeam.widvelo.location

/**
 * Classificação da qualidade do sinal GPS baseada na precisão reportada (em metros).
 */
enum class GpsSignalQuality(val label: String, val colorHex: String) {
    EXCELLENT("GPS Excelente", "#10B981"), // Verde esmeralda (< 8m)
    GOOD("GPS Bom", "#0284C7"),            // Azul elétrico (8m - 18m)
    MODERATE("GPS Moderado", "#F59E0B"),    // Âmbar (18m - 35m)
    POOR("GPS Fraco", "#EF4444"),           // Vermelho (> 35m)
    SEARCHING("Buscando Satélites...", "#94A3B8"); // Cinza (Sem sinal)

    companion object {
        fun fromAccuracy(accuracyMeters: Float?, lastUpdateAgeMillis: Long): GpsSignalQuality {
            if (accuracyMeters == null || lastUpdateAgeMillis > 6000L) {
                return SEARCHING
            }
            return when {
                accuracyMeters < 8f -> EXCELLENT
                accuracyMeters < 18f -> GOOD
                accuracyMeters < 35f -> MODERATE
                else -> POOR
            }
        }
    }
}
