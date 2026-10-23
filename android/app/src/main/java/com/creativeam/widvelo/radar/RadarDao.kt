package com.creativeam.widvelo.radar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RadarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(radars: List<RadarEntity>)

    @Query("""
        SELECT * FROM radars 
        WHERE latitude BETWEEN :minLat AND :maxLat 
          AND longitude BETWEEN :minLon AND :maxLon
    """)
    suspend fun getRadarsInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<RadarEntity>

    @Query("SELECT COUNT(*) FROM radars")
    suspend fun getRadarCount(): Int

    @Query("SELECT * FROM radars ORDER BY id ASC")
    suspend fun getAllRadars(): List<RadarEntity>

    @Query("DELETE FROM radars")
    suspend fun clearAll()
}
