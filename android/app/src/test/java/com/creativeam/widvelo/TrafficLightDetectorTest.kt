package com.creativeam.widvelo

import com.creativeam.widvelo.location.GpsSignalQuality
import com.creativeam.widvelo.location.SpeedUnit
import com.creativeam.widvelo.location.VehicleTelemetry
import com.creativeam.widvelo.trafficlight.TrafficLightAlert
import com.creativeam.widvelo.trafficlight.TrafficLightDao
import com.creativeam.widvelo.trafficlight.TrafficLightDetector
import com.creativeam.widvelo.trafficlight.TrafficLightEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TrafficLightDetectorTest {

    private lateinit var fakeDao: FakeTrafficLightDao
    private lateinit var detector: TrafficLightDetector

    @Before
    fun setup() {
        fakeDao = FakeTrafficLightDao()
        detector = TrafficLightDetector(fakeDao)
    }

    @Test
    fun testHaversineDistanceCalculation() {
        // Av. Paulista x Rua Augusta (-23.5583, -46.6588) e Av. Paulista x Frei Caneca (-23.5606, -46.6562)
        val dist = TrafficLightDetector.calculateHaversineDistance(
            -23.5583, -46.6588,
            -23.5606, -46.6562
        )
        // A distância real entre esses dois cruzamentos é aproximadamente 367 metros
        assertEquals(367.0, dist, 20.0)
    }

    @Test
    fun testTrafficLightDetectedWithin250Meters() = runBlocking {
        val light = TrafficLightEntity(
            id = "tl_test_1",
            latitude = -23.5583,
            longitude = -46.6588,
            roadName = "Av. Paulista",
            crossRoadName = "Rua Augusta",
            city = "São Paulo",
            cycleTimeSeconds = 60,
            directionBearing = 135f
        )
        fakeDao.items.add(light)

        // Veículo aproximando-se a ~100m, no mesmo rumo de 135° e a 45 km/h
        val telemetry = VehicleTelemetry(
            latitude = -23.5576,
            longitude = -46.6594,
            speedKmh = 45.0,
            bearing = 135f,
            accuracyMeters = 8.0,
            hasFix = true,
            isStandstill = false,
            signalQuality = GpsSignalQuality.EXCELLENT
        )

        val alert = detector.evaluate(telemetry)
        assertNotNull("Deveria detectar o semáforo à frente", alert)
        assertEquals("Av. Paulista x Rua Augusta", alert?.title)
        assertTrue("Distância deve ser menor que 250m", (alert?.distanceMeters ?: 999) < 250)
        assertFalse("Não deve estar em modo parado", alert?.isAtStandstill ?: true)
    }

    @Test
    fun testTrafficLightSuppressedOnCrossRoadAngle() = runBlocking {
        val light = TrafficLightEntity(
            id = "tl_test_perpendicular",
            latitude = -23.5583,
            longitude = -46.6588,
            roadName = "Rua Augusta",
            crossRoadName = "Av. Paulista",
            city = "São Paulo",
            cycleTimeSeconds = 60,
            directionBearing = 225f // Sentido perpendicular à Av. Paulista
        )
        fakeDao.items.add(light)

        // Veículo trafegando pela Av. Paulista a 135° (diferença angular de 90° com o semáforo)
        val telemetry = VehicleTelemetry(
            latitude = -23.5576,
            longitude = -46.6594,
            speedKmh = 45.0,
            bearing = 135f,
            accuracyMeters = 8.0,
            hasFix = true,
            isStandstill = false,
            signalQuality = GpsSignalQuality.EXCELLENT
        )

        val alert = detector.evaluate(telemetry)
        assertNull("Semáforo perpendicular deve ser filtrado e descartado", alert)
    }

    @Test
    fun testStandstillEngagesCountdownTimer() = runBlocking {
        val light = TrafficLightEntity(
            id = "tl_test_standstill",
            latitude = -23.5583,
            longitude = -46.6588,
            roadName = "Av. Paulista",
            crossRoadName = "Rua Augusta",
            city = "São Paulo",
            cycleTimeSeconds = 60,
            directionBearing = 135f
        )
        fakeDao.items.add(light)

        // Veículo totalmente parado a 20m do semáforo (Zero-Lock ativo: speed 0.0)
        val telemetry = VehicleTelemetry(
            latitude = -23.5582,
            longitude = -46.6589,
            speedKmh = 0.0,
            bearing = 135f,
            accuracyMeters = 5.0,
            hasFix = true,
            isStandstill = true,
            signalQuality = GpsSignalQuality.EXCELLENT
        )

        val alert = detector.evaluate(telemetry)
        assertNotNull(alert)
        assertTrue("Deve ativar modo de parada (Standstill)", alert?.isAtStandstill == true)
        assertTrue("Deve estimar segundos restantes do ciclo", (alert?.estimatedRemainingSeconds ?: 0) in 1..60)
    }

    private class FakeTrafficLightDao : TrafficLightDao {
        val items = mutableListOf<TrafficLightEntity>()

        override suspend fun getAll(): List<TrafficLightEntity> = items

        override suspend fun getTrafficLightsInBoundingBox(
            minLat: Double,
            maxLat: Double,
            minLng: Double,
            maxLng: Double
        ): List<TrafficLightEntity> {
            return items.filter {
                it.latitude in minLat..maxLat && it.longitude in minLng..maxLng
            }
        }

        override suspend fun getCount(): Int = items.size

        override suspend fun insertAll(trafficLights: List<TrafficLightEntity>) {
            items.addAll(trafficLights)
        }
    }
}
