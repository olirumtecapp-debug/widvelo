package com.creativeam.widvelo.incident

/**
 * Tipos de incidentes viários e anomalias de pista monitoradas pelo WidVelo.
 */
enum class RoadIncidentType(val badge: String, val label: String, val colorHex: String) {
    ROAD_CLOSURE("⛔", "Via Interditada", "#EF4444"),
    CONSTRUCTION("🚧", "Obras na Pista", "#F59E0B"),
    ACCIDENT("💥", "Acidente na Faixa", "#EF4444"),
    FLOODING("🌊", "Ponto de Alagamento", "#0284C7"),
    LANE_BLOCKED("⚠️", "Faixa Bloqueada", "#F59E0B")
}
