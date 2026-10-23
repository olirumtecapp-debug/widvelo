package com.creativeam.widvelo.trafficlight

import com.creativeam.widvelo.location.VehicleTelemetry
import kotlin.math.*

/**
 * Motor de detecção espacial de semáforos e gestão de temporizadores de ciclo de parada.
 */
class TrafficLightDetector(
    private val trafficLightDao: TrafficLightDao
) {

    private var standstillStartTimeMs: Long? = null
    private var activeTrafficLightId: String? = null

    /**
     * Avalia a telemetria atual do veículo e retorna o semáforo mais relevante à frente,
     * incluindo o temporizador de ciclo caso o condutor esteja imobilizado no cruzamento.
     */
    suspend fun evaluate(telemetry: VehicleTelemetry): TrafficLightAlert? {
        if (!telemetry.hasFix || telemetry.accuracyMeters > 35) {
            resetStandstill()
            return null
        }

        // Bounding box de ~800 metros em torno do veículo
        val latDelta = 0.007
        val lngDelta = 0.007

        val nearbyLights = trafficLightDao.getTrafficLightsInBoundingBox(
            minLat = telemetry.latitude - latDelta,
            maxLat = telemetry.latitude + latDelta,
            minLng = telemetry.longitude - lngDelta,
            maxLng = telemetry.longitude + lngDelta
        )

        if (nearbyLights.isEmpty()) {
            resetStandstill()
            return null
        }

        // Encontra o semáforo à frente mais próximo no mesmo sentido de deslocamento
        var closestAlert: TrafficLightAlert? = null
        var minDistance = Double.MAX_VALUE

        for (light in nearbyLights) {
            val distMeters = calculateHaversineDistance(
                telemetry.latitude,
                telemetry.longitude,
                light.latitude,
                light.longitude
            )

            // Limite de alerta: 250 metros
            if (distMeters > 250) continue

            // Validação de direção angular (cone de ±45° para vias urbanas)
            if (telemetry.speedKmh > 3.0f && light.directionBearing != null) {
                val bearingDiff = abs(telemetry.bearingDegrees - light.directionBearing) % 360
                val acuteAngle = if (bearingDiff > 180) 360 - bearingDiff else bearingDiff
                if (acuteAngle > 45.0f) {
                    continue // Semáforo na perpendicular ou fluxo oposto
                }
            }

            if (distMeters < minDistance) {
                minDistance = distMeters

                val isStandstill = telemetry.isStandstill && distMeters <= 65.0
                val (elapsedSec, remainingSec, progress) = processTimer(light, isStandstill)

                closestAlert = TrafficLightAlert(
                    entity = light,
                    distanceMeters = distMeters.roundToInt(),
                    isAtStandstill = isStandstill,
                    elapsedWaitingSeconds = elapsedSec,
                    estimatedRemainingSeconds = remainingSec,
                    progressFraction = progress
                )
            }
        }

        if (closestAlert == null || !closestAlert.isAtStandstill) {
            if (telemetry.speedKmh > 5.0f) {
                resetStandstill()
            }
        }

        return closestAlert
    }

    private fun processTimer(
        light: TrafficLightEntity,
        isStandstill: Boolean
    ): Triple<Int, Int, Float> {
        if (!isStandstill) {
            return Triple(0, 0, 0f)
        }

        val now = System.currentTimeMillis()
        if (standstillStartTimeMs == null || activeTrafficLightId != light.id) {
            standstillStartTimeMs = now
            activeTrafficLightId = light.id
        }

        val elapsedSec = ((now - (standstillStartTimeMs ?: now)) / 1000).toInt()
        val cycle = light.cycleTimeSeconds.coerceAtLeast(30)
        val remainingSec = (cycle - (elapsedSec % cycle)).coerceAtLeast(0)
        val progress = ((elapsedSec % cycle).toFloat() / cycle.toFloat()).coerceIn(0f, 1f)

        return Triple(elapsedSec, remainingSec, progress)
    }

    private fun resetStandstill() {
        standstillStartTimeMs = null
        activeTrafficLightId = null
    }

    companion object {
        private const val EARTH_RADIUS_METERS = 6371000.0

        fun calculateHaversineDistance(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val rLat1 = Math.toRadians(lat1)
            val rLat2 = Math.toRadians(lat2)

            val a = sin(dLat / 2).pow(2) +
                    cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return EARTH_RADIUS_METERS * c
        }
    }
}
