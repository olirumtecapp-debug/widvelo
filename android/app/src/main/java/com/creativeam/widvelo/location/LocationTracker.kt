package com.creativeam.widvelo.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/**
 * Gerenciador de localização e satélites do WidVelo.
 * Prioriza Google FusedLocationProviderClient e possui fallback automático para LocationManager
 * nativo do Android para garantir funcionamento 100% offline em qualquer aparelho.
 */
class LocationTracker(private val context: Context) {

    private val speedFilter = GpsSpeedFilter()
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _telemetry = MutableStateFlow(VehicleTelemetry())
    val telemetry: StateFlow<VehicleTelemetry> = _telemetry.asStateFlow()

    private var isTracking = false
    private var lastBearing: Float = 0f

    private val fusedCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { handleNewLocation(it) }
        }
    }

    private val nativeGpsListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            handleNewLocation(location)
        }
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (isTracking) return
        isTracking = true
        speedFilter.reset()

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 750L)
            .setMinUpdateIntervalMillis(500L)
            .setMinUpdateDistanceMeters(0f)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedClient.requestLocationUpdates(locationRequest, fusedCallback, Looper.getMainLooper())
                .addOnFailureListener {
                    startNativeGpsFallback()
                }
        } catch (e: Exception) {
            startNativeGpsFallback()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startNativeGpsFallback() {
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                750L,
                0f,
                nativeGpsListener,
                Looper.getMainLooper()
            )
        } catch (_: Exception) {
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        try {
            fusedClient.removeLocationUpdates(fusedCallback)
        } catch (_: Exception) {}

        try {
            locationManager?.removeUpdates(nativeGpsListener)
        } catch (_: Exception) {}

        _telemetry.value = _telemetry.value.copy(
            speedKmh = 0.0,
            speedDisplay = 0,
            isSessionActive = false,
            signalQuality = GpsSignalQuality.SEARCHING
        )
    }

    private fun handleNewLocation(location: Location) {
        val now = System.currentTimeMillis()
        val rawSpeedMps = if (location.hasSpeed()) location.speed else 0f
        val accuracy = if (location.hasAccuracy()) location.accuracy else 50f

        val filteredSpeedKmh = speedFilter.filter(
            rawSpeedMps = rawSpeedMps,
            accuracyMeters = accuracy,
            timestampMillis = location.time.takeIf { it > 0 } ?: now
        )

        // Se o veículo estiver em movimento acima de 5 km/h, atualiza o rumo (bearing).
        // Se estiver parado ou muito devagar, preserva o último rumo válido para o radar não perder o sentido.
        if (filteredSpeedKmh >= 5.0 && location.hasBearing() && location.bearing > 0f) {
            lastBearing = location.bearing
        }

        val quality = GpsSignalQuality.fromAccuracy(
            accuracyMeters = accuracy,
            lastUpdateAgeMillis = now - location.time
        )

        _telemetry.value = VehicleTelemetry(
            speedKmh = filteredSpeedKmh,
            speedDisplay = filteredSpeedKmh.roundToInt(),
            bearing = lastBearing,
            altitudeMeters = if (location.hasAltitude()) location.altitude else 0.0,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = accuracy,
            signalQuality = quality,
            timestampMillis = now,
            isSessionActive = isTracking,
            speedUnit = _telemetry.value.speedUnit
        )
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        _telemetry.value = _telemetry.value.copy(speedUnit = unit)
    }
}
