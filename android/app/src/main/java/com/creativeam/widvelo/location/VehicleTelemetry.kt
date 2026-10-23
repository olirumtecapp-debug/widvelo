package com.creativeam.widvelo.location

/**
 * Estado instantâneo da telemetria do veículo medido pelo GPS do WidVelo.
 */
data class VehicleTelemetry(
    val speedKmh: Double = 0.0,
    val speedDisplay: Int = 0,
    val bearing: Float = 0f,
    val altitudeMeters: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracyMeters: Float = 0f,
    val signalQuality: GpsSignalQuality = GpsSignalQuality.SEARCHING,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isSessionActive: Boolean = false,
    val speedUnit: SpeedUnit = SpeedUnit.KMH
) {
    val isStopped: Boolean get() = speedKmh <= 0.0

    val formattedSpeed: String
        get() = speedUnit.formatSpeed(speedKmh)
}
