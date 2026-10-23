package com.creativeam.widvelo

import android.app.Application
import com.creativeam.widvelo.radar.WidVeloDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Ponto de entrada do ciclo de vida da aplicação WidVelo.
 * Inicializa instâncias críticas de forma assíncrona para inicialização veloz e sem engasgos.
 */
class WidVeloApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Pré-aquece o banco de dados e sementes de radares em segundo plano
        applicationScope.launch {
            WidVeloDatabase.getInstance(applicationContext).radarDao().getRadarCount()
        }
    }

    companion object {
        lateinit var instance: WidVeloApplication
            private set
    }
}
