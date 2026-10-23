package com.creativeam.widvelo.trafficlight

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrafficLightDao {

    @Query("SELECT * FROM traffic_lights")
    suspend fun getAll(): List<TrafficLightEntity>

    @Query(
        """
        SELECT * FROM traffic_lights
        WHERE latitude BETWEEN :minLat AND :maxLat
          AND longitude BETWEEN :minLng AND :maxLng
    """
    )
    suspend fun getTrafficLightsInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): List<TrafficLightEntity>

    @Query("SELECT COUNT(*) FROM traffic_lights")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trafficLights: List<TrafficLightEntity>)
}
