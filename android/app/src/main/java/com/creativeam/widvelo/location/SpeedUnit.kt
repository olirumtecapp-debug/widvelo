package com.creativeam.widvelo.location

/**
 * Unidades de velocidade suportadas pelo WidVelo.
 */
enum class SpeedUnit(val label: String, val conversionFactorFromKmh: Double) {
    KMH("km/h", 1.0),
    MPH("mph", 0.621371);

    fun convertFromKmh(speedKmh: Double): Double {
        return speedKmh * conversionFactorFromKmh
    }

    fun formatSpeed(speedKmh: Double): String {
        val converted = convertFromKmh(speedKmh)
        return String.format(java.util.Locale.US, "%.0f", converted)
    }
}
