package com.creativeam.widvelo.trafficlight

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa um cruzamento semafórico georreferenciado mapeado no banco de dados local.
 * Dados originários de fontes públicas abertas (OpenStreetMap highway=traffic_signals e dados de engenharia de tráfego).
 */
@Entity(
    tableName = "traffic_lights",
    indices = [
        Index(value = ["latitude", "longitude"]),
        Index(value = ["city"]),
        Index(value = ["roadName"])
    ]
)
data class TrafficLightEntity(
    @PrimaryKey
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val roadName: String,
    val crossRoadName: String,
    val city: String,
    val hasRedLightCamera: Boolean = false,
    val cycleTimeSeconds: Int = 60,
    val directionBearing: Float? = null, // Azimute do fluxo da via (graus 0-360)
    val source: String = "OpenStreetMap / CET"
)
