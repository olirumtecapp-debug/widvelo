package com.creativeam.widvelo.overspeed

import com.creativeam.widvelo.speedlimit.RoadSpeedLimit

enum class OverspeedLevel(val label: String, val colorHex: String) {
    NO_LIMIT("Sem Limite Conhecido", "#64748B"),
    NORMAL("Dentro do Limite", "#10B981"),       // Verde
    CAUTION("Atenção (Margem de Tolerância)", "#F59E0B"), // Âmbar
    EXCEEDED("ACIMA DO LIMITE DA VIA!", "#EF4444") // Vermelho Alerta
}

data class OverspeedResult(
    val level: OverspeedLevel,
    val currentSpeedKmh: Double,
    val limitKmh: Int?,
    val excessKmh: Double,
    val shouldTriggerSound: Boolean
)

class OverspeedEvaluator(
    private var toleranceKmh: Int = 3,
    private val audioRepeatCooldownMillis: Long = 8000L
) {
    private var lastAudioTriggerTimeMillis: Long = 0L
    private var wasPreviouslyExceeded: Boolean = false

    fun setToleranceKmh(tolerance: Int) {
        this.toleranceKmh = tolerance.coerceIn(0, 20)
    }

    fun getToleranceKmh(): Int = toleranceKmh

    fun evaluate(currentSpeedKmh: Double, speedLimit: RoadSpeedLimit?): OverspeedResult {
        val limit = speedLimit?.takeIf { it.isAvailable }?.limitKmh

        if (limit == null) {
            wasPreviouslyExceeded = false
            return OverspeedResult(
                level = OverspeedLevel.NO_LIMIT,
                currentSpeedKmh = currentSpeedKmh,
                limitKmh = null,
                excessKmh = 0.0,
                shouldTriggerSound = false
            )
        }

        val excess = currentSpeedKmh - limit
        val now = System.currentTimeMillis()

        return when {
            excess <= 0.0 -> {
                wasPreviouslyExceeded = false
                OverspeedResult(
                    level = OverspeedLevel.NORMAL,
                    currentSpeedKmh = currentSpeedKmh,
                    limitKmh = limit,
                    excessKmh = 0.0,
                    shouldTriggerSound = false
                )
            }
            excess <= toleranceKmh -> {
                wasPreviouslyExceeded = false
                OverspeedResult(
                    level = OverspeedLevel.CAUTION,
                    currentSpeedKmh = currentSpeedKmh,
                    limitKmh = limit,
                    excessKmh = excess,
                    shouldTriggerSound = false
                )
            }
            else -> {
                // Excedeu o limite + a tolerância configurada pelo motorista
                val shouldBeep = !wasPreviouslyExceeded || (now - lastAudioTriggerTimeMillis >= audioRepeatCooldownMillis)
                if (shouldBeep) {
                    lastAudioTriggerTimeMillis = now
                }
                wasPreviouslyExceeded = true

                OverspeedResult(
                    level = OverspeedLevel.EXCEEDED,
                    currentSpeedKmh = currentSpeedKmh,
                    limitKmh = limit,
                    excessKmh = excess,
                    shouldTriggerSound = shouldBeep
                )
            }
        }
    }

    fun reset() {
        lastAudioTriggerTimeMillis = 0L
        wasPreviouslyExceeded = false
    }
}
