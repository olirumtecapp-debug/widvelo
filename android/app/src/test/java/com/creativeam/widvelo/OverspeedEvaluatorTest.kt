package com.creativeam.widvelo

import com.creativeam.widvelo.overspeed.OverspeedEvaluator
import com.creativeam.widvelo.overspeed.OverspeedLevel
import com.creativeam.widvelo.speedlimit.RoadSpeedLimit
import com.creativeam.widvelo.speedlimit.SpeedLimitStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OverspeedEvaluatorTest {

    private lateinit var evaluator: OverspeedEvaluator

    @Before
    fun setUp() {
        evaluator = OverspeedEvaluator(toleranceKmh = 3)
    }

    @Test
    fun testWithinSpeedLimit() {
        val limit = RoadSpeedLimit(limitKmh = 80, status = SpeedLimitStatus.CONFIRMED, roadName = "SP-160", source = "TEST")
        val result = evaluator.evaluate(currentSpeedKmh = 78.0, speedLimit = limit)

        assertEquals(OverspeedLevel.NORMAL, result.level)
        assertFalse(result.shouldTriggerSound)
        assertEquals(0.0, result.excessKmh, 0.001)
    }

    @Test
    fun testWithinToleranceMargin() {
        val limit = RoadSpeedLimit(limitKmh = 80, status = SpeedLimitStatus.CONFIRMED, roadName = "SP-160", source = "TEST")
        // Velocidade 82 km/h com limite 80 e tolerância 3 -> dentro da tolerância
        val result = evaluator.evaluate(currentSpeedKmh = 82.0, speedLimit = limit)

        assertEquals(OverspeedLevel.CAUTION, result.level)
        assertFalse("Não deve tocar alarme irritante dentro da tolerância", result.shouldTriggerSound)
        assertEquals(2.0, result.excessKmh, 0.001)
    }

    @Test
    fun testExceedingSpeedLimitAboveTolerance() {
        val limit = RoadSpeedLimit(limitKmh = 80, status = SpeedLimitStatus.CONFIRMED, roadName = "SP-160", source = "TEST")
        // Velocidade 86 km/h com limite 80 e tolerância 3 -> Excedido!
        val result = evaluator.evaluate(currentSpeedKmh = 86.0, speedLimit = limit)

        assertEquals(OverspeedLevel.EXCEEDED, result.level)
        assertTrue("Deve disparar aviso sonoro ao ultrapassar a margem de segurança", result.shouldTriggerSound)
        assertEquals(6.0, result.excessKmh, 0.001)
    }

    @Test
    fun testNoSpeedLimitAvailable() {
        val result = evaluator.evaluate(currentSpeedKmh = 100.0, speedLimit = RoadSpeedLimit.UNAVAILABLE)

        assertEquals(OverspeedLevel.NO_LIMIT, result.level)
        assertFalse("Nunca deve apitar excesso quando o limite for desconhecido", result.shouldTriggerSound)
    }
}
