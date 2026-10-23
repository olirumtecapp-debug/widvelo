package com.creativeam.widvelo.radar

/**
 * Categorias de fiscalização eletrônica catalogadas pelo WidVelo.
 */
enum class RadarType(val label: String, val shortBadge: String) {
    FIXED("Radar Fixo", "FIXO"),
    SPEED_TRAP("Radar Móvel / Portátil", "MÓVEL"),
    RED_LIGHT("Semáforo com Radar", "SEMÁFORO"),
    SECTION_AVERAGE("Velocidade Média de Trecho", "MÉDIA"),
    SURVEILLANCE("Fiscalização Eletrônica / Balança", "FISCALIZAÇÃO");

    companion object {
        fun fromString(value: String?): RadarType {
            if (value.isNullOrBlank()) return FIXED
            return try {
                valueOf(value.uppercase())
            } catch (_: Exception) {
                when {
                    value.contains("semaforo", ignoreCase = true) || value.contains("semáforo", ignoreCase = true) || value.contains("sinal", ignoreCase = true) -> RED_LIGHT
                    value.contains("movel", ignoreCase = true) || value.contains("móvel", ignoreCase = true) || value.contains("portatil", ignoreCase = true) -> SPEED_TRAP
                    value.contains("media", ignoreCase = true) || value.contains("média", ignoreCase = true) -> SECTION_AVERAGE
                    value.contains("balanca", ignoreCase = true) || value.contains("balança", ignoreCase = true) -> SURVEILLANCE
                    else -> FIXED
                }
            }
        }
    }
}
