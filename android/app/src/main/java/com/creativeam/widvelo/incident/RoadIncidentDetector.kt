package com.creativeam.widvelo.incident

import com.creativeam.widvelo.location.VehicleTelemetry
import com.creativeam.widvelo.radar.RadarDetector
import kotlin.math.abs

/**
 * Motor espacial para identificação de interdições e incidentes à frente na rota.
 */
class RoadIncidentDetector(
    private val incidentDao: RoadIncidentDao
) {

    suspend fun evaluate(telemetry: VehicleTelemetry): RoadIncidentAlert? {
        if ((telemetry.latitude == 0.0 && telemetry.longitude == 0.0) || telemetry.accuracyMeters > 40f) {
            return null
        }

        // Bounding box de ~1.5 km ao redor
        val nearbyIncidents = incidentDao.getIncidentsInBoundingBox(
            minLat = telemetry.latitude - 0.015,
            maxLat = telemetry.latitude + 0.015,
            minLon = telemetry.longitude - 0.015,
            maxLon = telemetry.longitude + 0.015
        )

        if (nearbyIncidents.isEmpty()) return null

        var closestAlert: RoadIncidentAlert? = null
        var minDistance = Double.MAX_VALUE

        for (inc in nearbyIncidents) {
            val dist = RadarDetector.haversineDistanceMeters(
                telemetry.latitude, telemetry.longitude,
                inc.latitude, inc.longitude
            )

            // Limite de alerta de incidente: até 800 metros
            if (dist > 800.0) continue

            // Verifica se está à frente no cone de visão
            val isAhead = if (telemetry.speedKmh >= 5.0 && inc.directionBearing != null) {
                val diff = abs(telemetry.bearing - inc.directionBearing) % 360f
                val acute = if (diff > 180f) 360f - diff else diff
                acute <= 65f
            } else {
                true
            }

            if (isAhead && dist < minDistance) {
                minDistance = dist
                closestAlert = RoadIncidentAlert(
                    entity = inc,
                    distanceMeters = dist.toInt(),
                    isAhead = true
                )
            }
        }

        return closestAlert
    }
}
