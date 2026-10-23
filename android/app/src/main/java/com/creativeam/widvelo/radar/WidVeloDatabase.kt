package com.creativeam.widvelo.radar

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [RadarEntity::class], version = 1, exportSchema = false)
@TypeConverters(RadarTypeConverter::class)
abstract class WidVeloDatabase : RoomDatabase() {

    abstract fun radarDao(): RadarDao

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
                            // Popula o banco com os radares homologados do Brasil na primeira inicialização
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).radarDao().insertAll(PreloadedRadars.getInitialSeed())
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
