package com.creativeam.widvelo.radar

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.creativeam.widvelo.incident.IncidentTypeConverter
import com.creativeam.widvelo.incident.PreloadedIncidents
import com.creativeam.widvelo.incident.RoadIncidentDao
import com.creativeam.widvelo.incident.RoadIncidentEntity
import com.creativeam.widvelo.trafficlight.PreloadedTrafficLights
import com.creativeam.widvelo.trafficlight.TrafficLightDao
import com.creativeam.widvelo.trafficlight.TrafficLightEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RadarEntity::class, TrafficLightEntity::class, RoadIncidentEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(RadarTypeConverter::class, IncidentTypeConverter::class)
abstract class WidVeloDatabase : RoomDatabase() {

    abstract fun radarDao(): RadarDao
    abstract fun trafficLightDao(): TrafficLightDao
    abstract fun roadIncidentDao(): RoadIncidentDao

    companion object {
        @Volatile
        private var INSTANCE: WidVeloDatabase? = null

        fun getInstance(context: Context): WidVeloDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WidVeloDatabase::class.java,
                    "widvelo_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                ensureSeeded(database)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Garante que os radares, semáforos e incidentes estejam sempre presentes no banco,
         * prevenindo tabelas vazias por falhas assíncronas ou migrações.
         */
        suspend fun ensureSeeded(database: WidVeloDatabase) {
            try {
                if (database.radarDao().getRadarCount() == 0) {
                    database.radarDao().insertAll(PreloadedRadars.getInitialSeed())
                }
                if (database.trafficLightDao().getCount() == 0) {
                    database.trafficLightDao().insertAll(PreloadedTrafficLights.getInitialSeed())
                }
                if (database.roadIncidentDao().getCount() == 0) {
                    database.roadIncidentDao().insertAll(PreloadedIncidents.getInitialSeed())
                }
            } catch (_: Exception) {}
        }
    }
}
