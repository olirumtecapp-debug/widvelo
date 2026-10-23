package com.creativeam.widvelo.radar

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.creativeam.widvelo.trafficlight.PreloadedTrafficLights
import com.creativeam.widvelo.trafficlight.TrafficLightDao
import com.creativeam.widvelo.trafficlight.TrafficLightEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RadarEntity::class, TrafficLightEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(RadarTypeConverter::class)
abstract class WidVeloDatabase : RoomDatabase() {

    abstract fun radarDao(): RadarDao
    abstract fun trafficLightDao(): TrafficLightDao

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
                            // Popula o banco com radares e semáforos homologados na primeira inicialização
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.radarDao().insertAll(PreloadedRadars.getInitialSeed())
                                database.trafficLightDao().insertAll(PreloadedTrafficLights.getInitialSeed())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
