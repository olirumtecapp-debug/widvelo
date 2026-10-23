package com.creativeam.widvelo.speedlimit

import com.creativeam.widvelo.radar.RadarAlert
import com.creativeam.widvelo.radar.RadarDao
import com.creativeam.widvelo.radar.RadarDetector

/**
 * Motor de reconhecimento do limite de velocidade da via.
 * Hierarquia de Resolução:
 * 1. Radar confirmado à frente -> Limite exato fiscalizado (100% de confiança).
 * 2. Corredor próximo (radar homologado a até 1.500m) -> Herda o limite da via.
 * 3. Velocidade de rodovia (>= 75 km/h) -> Limite regulamentar de rodovia (90 km/h).
 * 4. Padrão Urbano Regulamentar (Art. 61 CTB / Municipal SP e SBC) -> 50 km/h (ou 60 km/h se arterial).
 */
class SpeedLimitEngine(private val radarDao: RadarDao) {

    private var defaultUrbanLimitKmh: Int = 50
    private var lastKnownLimit: RoadSpeedLimit = RoadSpeedLimit.UNAVAILABLE

    fun setDefaultUrbanLimit(limit: Int) {
        defaultUrbanLimitKmh = limit
    }

    /**
     * Resolve o limite de velocidade atual.
     */
    suspend fun resolveSpeedLimit(
        latitude: Double,
        longitude: Double,
        currentSpeedKmh: Double,
        activeRadarAlert: RadarAlert?
    ): RoadSpeedLimit {
        // 1. Radar homologado à frente
        if (activeRadarAlert != null && activeRadarAlert.radar.speedLimit != null) {
            val resolved = RoadSpeedLimit(
                limitKmh = activeRadarAlert.radar.speedLimit,
                status = SpeedLimitStatus.CONFIRMED,
                roadName = activeRadarAlert.radar.highway ?: "Via Monitorada",
                source = "RADAR_${activeRadarAlert.radar.source}",
                confidence = 1.0f
            )
            lastKnownLimit = resolved
            return resolved
        }

        // 2. Busca de corredores homologados na área (até 1.500m)
        if (currentSpeedKmh >= 5.0) {
            val nearbyRadars = radarDao.getRadarsInBoundingBox(
                minLat = latitude - 0.02,
                maxLat = latitude + 0.02,
                minLon = latitude - 0.02,
                maxLon = latitude + 0.02
            )

            val closestWithLimit = nearbyRadars
                .filter { it.speedLimit != null }
                .minByOrNull { RadarDetector.haversineDistanceMeters(latitude, longitude, it.latitude, it.longitude) }

            if (closestWithLimit != null) {
                val dist = RadarDetector.haversineDistanceMeters(latitude, longitude, closestWithLimit.latitude, closestWithLimit.longitude)
                if (dist <= 1500.0) {
                    val resolved = RoadSpeedLimit(
                        limitKmh = closestWithLimit.speedLimit,
                        status = SpeedLimitStatus.CONFIRMED,
                        roadName = closestWithLimit.highway ?: "Via Principal",
                        source = "CORREDOR_${closestWithLimit.source}",
                        confidence = 0.95f
                    )
                    lastKnownLimit = resolved
                    return resolved
                }
            }
        }

        // 3. Critério de Rodovia por velocidade contínua
        if (currentSpeedKmh >= 78.0) {
            val resolved = RoadSpeedLimit(
                limitKmh = 90,
                status = SpeedLimitStatus.CONFIRMED,
                roadName = "Rodovia / Via Expressa",
                source = "CTB_RODOVIA",
                confidence = 0.85f
            )
            lastKnownLimit = resolved
            return resolved
        }

        // 4. Padrão Urbano Regulamentado (Art. 61 CTB - Padrão SBC e Capitais)
        val urbanLimit = if (currentSpeedKmh > 54.0 && currentSpeedKmh < 78.0) 60 else defaultUrbanLimitKmh
        val resolved = RoadSpeedLimit(
            limitKmh = urbanLimit,
            status = SpeedLimitStatus.CONFIRMED,
            roadName = "Via Urbana Regulamentada",
            source = "PADRAO_URBANO",
            confidence = 0.80f
        )
        lastKnownLimit = resolved
        return resolved
    }

    fun getLastKnownLimit(): RoadSpeedLimit = lastKnownLimit

    fun clearCache() {
        lastKnownLimit = RoadSpeedLimit.UNAVAILABLE
    }
}
