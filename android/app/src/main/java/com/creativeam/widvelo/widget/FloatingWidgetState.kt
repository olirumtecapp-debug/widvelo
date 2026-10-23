package com.creativeam.widvelo.widget

import com.creativeam.widvelo.location.GpsSignalQuality
import com.creativeam.widvelo.overspeed.OverspeedLevel

/**
 * Estado transmitido para o Widget Flutuante do WidVelo.
 */
data class FloatingWidgetState(
    val speedValue: String = "0",
    val speedUnit: String = "km/h",
    val speedLimitText: String? = null,
    val radarDistanceText: String? = null,
    val radarTypeLabel: String? = null,
    val trafficLightNotice: String? = null,
    val isAtRedLightStandstill: Boolean = false,
    val trafficLightProgress: Float = 0f,
    val overspeedLevel: OverspeedLevel = OverspeedLevel.NORMAL,
    val isRadarImminent: Boolean = false,
    val gpsSignalQuality: GpsSignalQuality = GpsSignalQuality.SEARCHING,
    val isSessionActive: Boolean = false
)
