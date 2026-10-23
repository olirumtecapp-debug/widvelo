package com.creativeam.widvelo.speedlimit

enum class SpeedLimitStatus(val label: String) {
    CONFIRMED("Limite Confirmado"),
    UNCONFIRMED("Limite Não Confirmado"),
    UNAVAILABLE("Limite Não Disponível")
}

data class RoadSpeedLimit(
    val limitKmh: Int?,
    val status: SpeedLimitStatus,
    val roadName: String?,
    val source: String,
    val confidence: Float = 1.0f
) {
    val isAvailable: Boolean get() = limitKmh != null && status == SpeedLimitStatus.CONFIRMED

    val displayText: String
        get() = when {
            isAvailable -> "$limitKmh"
            status == SpeedLimitStatus.UNCONFIRMED -> "Não confirmado"
            else -> "Não disponível"
        }

    companion object {
        val UNAVAILABLE = RoadSpeedLimit(
            limitKmh = null,
            status = SpeedLimitStatus.UNAVAILABLE,
            roadName = null,
            source = "SISTEMA"
        )
    }
}
