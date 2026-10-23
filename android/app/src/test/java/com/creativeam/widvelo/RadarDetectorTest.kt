package com.creativeam.widvelo

import com.creativeam.widvelo.radar.PreloadedRadars
import com.creativeam.widvelo.radar.RadarDetector
import com.creativeam.widvelo.radar.RadarEntity
import com.creativeam.widvelo.radar.RadarType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarDetectorTest {

    @Test
    fun testHaversineDistance() {
        // Marco Zero de São Paulo (-23.5505, -46.6333) até Ponte das Bandeiras (-23.5186, -46.6253)
        // Distância real em linha reta ~3.6 km (3600 metros)
        val dist = RadarDetector.haversineDistanceMeters(
            -23.5505, -46.6333,
            -23.5186, -46.6253
        )
        assertTrue("Distância deve ser em torno de 3600m", dist in 3400.0..3800.0)
    }

    @Test
    fun testBearingCompatibilitySameDirection() {
        // Veículo viajando para o Leste (90°)
        val vehicleBearing = 90f
        val radarBearingSameDirection = 85f // Diferença de apenas 5 graus

        val isCompatible = RadarDetector.isBearingCompatible(vehicleBearing, radarBearingSameDirection, toleranceDegrees = 35f)
        assertTrue("Radares no mesmo sentido devem ser aceitos", isCompatible)
    }

    @Test
    fun testBearingCompatibilityOppositeDirectionSuppression() {
        // Veículo viajando para o Leste (90°).
        // Radar fiscaliza quem está vindo no sentido contrário (Oeste = 270°).
        val vehicleBearing = 90f
        val radarBearingOppositeDirection = 270f

        val isCompatible = RadarDetector.isBearingCompatible(vehicleBearing, radarBearingOppositeDirection, toleranceDegrees = 35f)
        assertFalse("Radares da pista contrária NUNCA devem apitar", isCompatible)
    }

    @Test
    fun testRadarBehindVehicleIsSuppressed() {
        // Carro em São Paulo indo para o Norte (-23.55, -46.63) com rumo 0° (Norte)
        // Radar está ao Sul (-23.58, -46.63) -> atrás do carro!
        val isAhead = RadarDetector.isAheadOfVehicle(
            vehicleLat = -23.55, vehicleLon = -46.63,
            vehicleBearing = 0f,
            radarLat = -23.58, radarLon = -46.63
        )
        assertFalse("Radar que já ficou para trás não deve emitir alerta", isAhead)
    }

    @Test
    fun testEvaluateRadarRealLifeScenario() {
        // Radar na Dutra km 214 (-23.4421, -46.4950), sentido Rio de Janeiro (~70°)
        val radar = RadarEntity(
            id = "TEST-DUTRA",
            latitude = -23.4421,
            longitude = -46.4950,
            speedLimit = 90,
            type = RadarType.FIXED,
            bearing = 70f,
            highway = "BR-116",
            kmReference = "km 214",
            municipality = "Guarulhos",
            state = "SP",
            source = "ANTT",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-10-01"
        )

        // Carro a 300 metros antes do radar, trafegando a 85 km/h no rumo 68° (alinhado com a via)
        // Coordenada ~300m antes: (-23.4430, -46.4975)
        val alert = RadarDetector.evaluateRadar(
            vehicleLat = -23.4430,
            vehicleLon = -46.4975,
            vehicleSpeedKmh = 85.0,
            vehicleBearing = 68f,
            radar = radar,
            highwayAlertDistanceMeters = 600
        )

        assertNotNull("Deve gerar alerta para radar à frente no mesmo sentido", alert)
        assertEquals(90, alert!!.radar.speedLimit)
        assertTrue("Distância deve estar no raio de aproximação", alert.distanceMeters in 200..450)
    }
}
