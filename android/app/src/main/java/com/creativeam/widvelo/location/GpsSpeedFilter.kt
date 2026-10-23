package com.creativeam.widvelo.location

import kotlin.math.abs

/**
 * Filtro de velocidade GPS de alta fidelidade para o WidVelo.
 *
 * Elimina o "jitter" (oscilação estática de 1 a 3 km/h quando parado no semáforo),
 * filtra saltos irreais causados por desvio de satélite e responde com agilidade
 * durante acelerações e frenagens fortes.
 */
class GpsSpeedFilter(
    private val standstillThresholdKmh: Double = 1.5,
    private val maxRealisticAccelerationKmhPerSec: Double = 35.0
) {
    private var lastFilteredSpeedKmh: Double = 0.0
    private var lastTimestampMillis: Long = 0L

    /**
     * Processa a velocidade bruta do GPS (em m/s) e a precisão do ponto.
     * Retorna a velocidade filtrada e estável em km/h.
     */
    fun filter(rawSpeedMps: Float, accuracyMeters: Float, timestampMillis: Long): Double {
        val rawSpeedKmh = (rawSpeedMps * 3.6).coerceAtLeast(0.0)

        // 1. Trava de Imobilidade (Zero-Lock):
        // Se a velocidade for menor que 1.5 km/h ou se o GPS reportou 0.0 m/s com precisão razoável,
        // força 0.0 km/h cravado imediatamente.
        if (rawSpeedKmh < standstillThresholdKmh) {
            lastFilteredSpeedKmh = 0.0
            lastTimestampMillis = timestampMillis
            return 0.0
        }

        // Se for a primeira leitura válida
        if (lastTimestampMillis == 0L) {
            lastFilteredSpeedKmh = rawSpeedKmh
            lastTimestampMillis = timestampMillis
            return rawSpeedKmh
        }

        val elapsedSec = ((timestampMillis - lastTimestampMillis) / 1000.0).coerceIn(0.1, 5.0)
        lastTimestampMillis = timestampMillis

        // 2. Proteção contra saltos físicos impossíveis (teletransporte / erro de multipath):
        val deltaSpeed = rawSpeedKmh - lastFilteredSpeedKmh
        val maxAllowedDelta = maxRealisticAccelerationKmhPerSec * elapsedSec
        val clampedTargetSpeed = if (abs(deltaSpeed) > maxAllowedDelta) {
            if (deltaSpeed > 0) lastFilteredSpeedKmh + maxAllowedDelta else lastFilteredSpeedKmh - maxAllowedDelta
        } else {
            rawSpeedKmh
        }

        // 3. Coeficiente adaptativo (Alpha) do filtro passa-baixa (EMA):
        // - Frenagem ou aceleração rápida: alpha alto (0.80) para resposta instantânea.
        // - Velocidade de cruzeiro estável: alpha menor (0.45) para absorver o tremor do satélite.
        // - Sinal GPS impreciso (> 25m): reduz a sensibilidade para não seguir ruído.
        val isDynamicManeuver = abs(clampedTargetSpeed - lastFilteredSpeedKmh) > 5.0
        var alpha = if (isDynamicManeuver) 0.80 else 0.45

        if (accuracyMeters > 25f) {
            alpha *= 0.60 // amortece ruído de satélite fraco
        }

        val filtered = (alpha * clampedTargetSpeed) + ((1.0 - alpha) * lastFilteredSpeedKmh)
        val finalSpeed = if (filtered < standstillThresholdKmh) 0.0 else filtered

        lastFilteredSpeedKmh = finalSpeed
        return finalSpeed
    }

    /**
     * Reseta o estado interno do filtro (por exemplo, ao iniciar ou reiniciar uma sessão).
     */
    fun reset() {
        lastFilteredSpeedKmh = 0.0
        lastTimestampMillis = 0L
    }

    fun getCurrentSpeedKmh(): Double = lastFilteredSpeedKmh
}
