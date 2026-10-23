package com.creativeam.widvelo.speedlimit

import com.creativeam.widvelo.radar.RadarAlert
import com.creativeam.widvelo.radar.RadarDao
import com.creativeam.widvelo.radar.RadarDetector

/**
 * Motor de reconhecimento do limite de velocidade da via.
 * Segue a diretriz rigorosa: NUNCA inventa limites se não houver dados comprovados.
 */
class SpeedLimitEngine(private val radarDao: RadarDao) {

    private var lastKnownLimit: RoadSpeedLimit = RoadSpeedLimit.UNAVAILABLE

    /**
     * Resolve o limite de velocidade atual baseado na proximidade de radares conhecidos
     * e no histórico do trecho da via.
     */
    suspend fun resolveSpeedLimit(
        latitude: Double,
        longitude: Double,
        currentSpeedKmh: Double,
        activeRadarAlert: RadarAlert?
    ): RoadSpeedLimit {
        // 1. Se estamos nos aproximando de um radar homologado com limite cadastrado:
        // A velocidade do radar é a verdade absoluta daquele trecho da via!
        if (activeRadarAlert != null && activeRadarAlert.radar.speedLimit != null) {
            val resolved = RoadSpeedLimit(
                limitKmh = activeRadarAlert.radar.speedLimit,
                status = SpeedLimitStatus.CONFIRMED,
                roadName = activeRadarAlert.radar.highway,
                source = "RADAR_${activeRadarAlert.radar.source}",
                confidence = 1.0f
            )
            lastKnownLimit = resolved
            return resolved
        }

        // 2. Se o veículo está em movimento, busca radares recentes a até 1.200m para herdar o limite do corredor
        if (currentSpeedKmh >= 10.0) {
            val nearbyRadars = radarDao.getRadarsInBoundingBox(
                minLat = latitude - 0.015,
                maxLat = latitude + 0.015,
                minLon = longitude - 0.015,
                maxLon = longitude + 0.015
            )

            val closestWithLimit = nearbyRadars
                .filter { it.speedLimit != null }
                .minByOrNull { RadarDetector.haversineDistanceMeters(latitude, longitude, it.latitude, it.longitude) }

            if (closestWithLimit != null) {
                val dist = RadarDetector.haversineDistanceMeters(latitude, longitude, closestWithLimit.latitude, closestWithLimit.longitude)
                if (dist <= 1200.0) {
                    val resolved = RoadSpeedLimit(
                        limitKmh = closestWithLimit.speedLimit,
                        status = SpeedLimitStatus.CONFIRMED,
                        roadName = closestWithLimit.highway,
                        source = "TRECHO_${closestWithLimit.source}",
                        confidence = 0.90f
                    )
                    lastKnownLimit = resolved
                    return resolved
                }
            }
        }

        // 3. Se nenhuma fonte autorizada cobrir este trecho:
        return RoadSpeedLimit.UNAVAILABLE
    }

    fun getLastKnownLimit(): RoadSpeedLimit = lastKnownLimit

    fun clearCache() {
        lastKnownLimit = RoadSpeedLimit.UNAVAILABLE
    }
}
