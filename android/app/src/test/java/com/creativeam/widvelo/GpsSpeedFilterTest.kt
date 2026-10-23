package com.creativeam.widvelo

import com.creativeam.widvelo.location.GpsSpeedFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GpsSpeedFilterTest {

    private lateinit var filter: GpsSpeedFilter

    @Before
    fun setUp() {
        filter = GpsSpeedFilter(standstillThresholdKmh = 1.5)
    }

    @Test
    fun testConversionMpsToKmh() {
        // 10 m/s = 36 km/h
        val speed = filter.filter(rawSpeedMps = 10f, accuracyMeters = 5f, timestampMillis = 1000L)
        assertEquals(36.0, speed, 0.5)
    }

    @Test
    fun testStandstillZeroLock() {
        // 0.3 m/s = 1.08 km/h -> abaixo do limiar de 1.5 km/h -> deve travar em 0.0 km/h
        val stopped = filter.filter(rawSpeedMps = 0.3f, accuracyMeters = 5f, timestampMillis = 1000L)
        assertEquals(0.0, stopped, 0.001)

        // 0.0 m/s -> 0.0 km/h
        val zero = filter.filter(rawSpeedMps = 0.0f, accuracyMeters = 3f, timestampMillis = 2000L)
        assertEquals(0.0, zero, 0.001)
    }

    @Test
    fun testAccelerationSmoothing() {
        // Inicia a 40 km/h (~11.11 m/s)
        filter.filter(rawSpeedMps = 11.11f, accuracyMeters = 5f, timestampMillis = 1000L)

        // Acelera gradualmente para 60 km/h (~16.66 m/s) em 1 segundo
        val next = filter.filter(rawSpeedMps = 16.66f, accuracyMeters = 5f, timestampMillis = 2000L)
        assertTrue("A velocidade deve responder à aceleração progressiva", next > 40.0 && next <= 60.0)
    }

    @Test
    fun testMultipathSpikeRejection() {
        // Veículo está a 50 km/h (~13.88 m/s)
        filter.filter(rawSpeedMps = 13.88f, accuracyMeters = 5f, timestampMillis = 1000L)

        // Um glitch absurdo de satélite reporta 200 km/h em 200ms
        val clamped = filter.filter(rawSpeedMps = 55.55f, accuracyMeters = 30f, timestampMillis = 1200L)
        assertTrue("Salto irreal deve ser amortecido contra picos de teletransporte", clamped < 75.0)
    }
}
