package com.creativeam.widvelo.radar

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Motor de detecção geográfica de radares do WidVelo.
 * Utiliza trigonometria esférica (fórmula de Haversine) e comparação angular de azimute
 * para evitar falsos positivos de pistas contrárias ou vias paralelas.
 */
object RadarDetector {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calcula a distância geodésica em metros entre dois pontos geográficos.
     */
    fun haversineDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(rLat1) * cos(rLat2) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calcula o azimute/rumo inicial (bearing) em graus (0° a 359°) do Ponto 1 para o Ponto 2.
     */
    fun calculateBearing(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val dLon = Math.toRadians(lon2 - lon1)

        val y = sin(dLon) * cos(rLat2)
        val x = cos(rLat1) * sin(rLat2) - sin(rLat1) * cos(rLat2) * cos(dLon)
        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        return ((bearingDeg + 360.0) % 360.0).toFloat()
    }

    /**
     * Verifica se a direção de deslocamento do veículo coincide com o sentido de fiscalização do radar.
     * Retorna true se estiver alinhado dentro da tolerância angular (cone de abertura).
     */
    fun isBearingCompatible(
        vehicleBearing: Float,
        radarBearing: Float?,
        toleranceDegrees: Float = 40f
    ): Boolean {
        // Se o radar não tiver direção cadastrada, atua como omnidirecional (ex: semáforo de cruzamento)
        if (radarBearing == null) return true

        val diff = abs(vehicleBearing - radarBearing) % 360f
        val smallestAngle = if (diff > 180f) 360f - diff else diff
        return smallestAngle <= toleranceDegrees
    }

    /**
     * Avalia se o radar está à frente do veículo e não atrás.
     */
    fun isAheadOfVehicle(
        vehicleLat: Double, vehicleLon: Double,
        vehicleBearing: Float,
        radarLat: Double, radarLon: Double,
        toleranceDegrees: Float = 75f
    ): Boolean {
        val bearingToRadar = calculateBearing(vehicleLat, vehicleLon, radarLat, radarLon)
        val diff = abs(vehicleBearing - bearingToRadar) % 360f
        val smallestAngle = if (diff > 180f) 360f - diff else diff
        return smallestAngle <= toleranceDegrees
    }

    /**
     * Avalia um radar específico em relação à posição e movimento do veículo.
     */
    fun evaluateRadar(
        vehicleLat: Double,
        vehicleLon: Double,
        vehicleSpeedKmh: Double,
        vehicleBearing: Float,
        radar: RadarEntity,
        highwayAlertDistanceMeters: Int = 600,
        urbanAlertDistanceMeters: Int = 350
    ): RadarAlert? {
        val distance = haversineDistanceMeters(vehicleLat, vehicleLon, radar.latitude, radar.longitude).toInt()
        val isHighway = vehicleSpeedKmh >= 70.0 || (radar.highway?.startsWith("BR", ignoreCase = true) == true || radar.highway?.startsWith("SP", ignoreCase = true) == true)
        val alertThreshold = if (isHighway) highwayAlertDistanceMeters else urbanAlertDistanceMeters

        // Se estiver fora do raio de interesse do radar
        if (distance > alertThreshold + 200) {
            return null
        }

        // Se o veículo estiver em movimento (> 5 km/h), filtra por direção
        val directionalMatch = if (vehicleSpeedKmh >= 5.0) {
            val ahead = isAheadOfVehicle(vehicleLat, vehicleLon, vehicleBearing, radar.latitude, radar.longitude)
            val compatible = isBearingCompatible(vehicleBearing, radar.bearing)
            ahead && compatible
        } else {
            true // Parado ou em manobra lenta
        }

        if (!directionalMatch) {
            return null
        }

        val stage = when {
            distance <= 100 -> AlertStage.IMMINENT
            distance <= alertThreshold -> AlertStage.APPROACH
            else -> AlertStage.FAR
        }

        return RadarAlert(
            radar = radar,
            distanceMeters = distance,
            isDirectionalMatch = true,
            isImminent = distance <= 100,
            alertStage = stage
        )
    }
}
