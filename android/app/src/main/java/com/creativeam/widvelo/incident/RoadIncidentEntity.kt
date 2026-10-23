package com.creativeam.widvelo.incident

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "road_incidents")
data class RoadIncidentEntity(
    @PrimaryKey
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val type: RoadIncidentType,
    val roadName: String,
    val crossRoadName: String?,
    val municipality: String,
    val description: String,
    val directionBearing: Float? = null,
    val isActive: Boolean = true,
    val reportedAt: String
)

class IncidentTypeConverter {
    @TypeConverter
    fun fromType(type: RoadIncidentType): String = type.name

    @TypeConverter
    fun toType(value: String): RoadIncidentType {
        return try {
            RoadIncidentType.valueOf(value)
        } catch (_: Exception) {
            RoadIncidentType.CONSTRUCTION
        }
    }
}
