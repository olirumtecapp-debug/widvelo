package com.creativeam.widvelo.incident

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoadIncidentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(incidents: List<RoadIncidentEntity>)

    @Query("""
        SELECT * FROM road_incidents
        WHERE isActive = 1
          AND latitude BETWEEN :minLat AND :maxLat
          AND longitude BETWEEN :minLon AND :maxLon
    """)
    suspend fun getIncidentsInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<RoadIncidentEntity>

    @Query("SELECT COUNT(*) FROM road_incidents WHERE isActive = 1")
    suspend fun getCount(): Int

    @Query("SELECT * FROM road_incidents ORDER BY id ASC")
    suspend fun getAll(): List<RoadIncidentEntity>

    @Query("DELETE FROM road_incidents")
    suspend fun clearAll()
}
