package com.creativeam.widvelo.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.creativeam.widvelo.WidVeloApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Serviço em primeiro plano (Foreground Service) do WidVelo.
 * Mantém o GPS ativo e estável mesmo quando o usuário troca para o Waze, Google Maps ou bloqueia a tela.
 */
class WidVeloLocationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var tracker: LocationTracker
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        tracker = LocationTracker(applicationContext)
        createNotificationChannel()

        serviceScope.launch {
            tracker.telemetry.collect { telemetry ->
                if (!_isSimulationRunning.value) {
                    _currentTelemetry.value = telemetry
                    updateNotification(telemetry)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SESSION -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_SESSION, null -> {
                val notification = buildNotification(_currentTelemetry.value)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
                tracker.startTracking()
                _isServiceRunning.value = true
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        tracker.stopTracking()
        _isServiceRunning.value = false
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WidVelo - Velocímetro & Radares em Execução",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantém o velocímetro e alertas de radares ativos durante a condução"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(telemetry: VehicleTelemetry): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, WidVeloLocationService::class.java).apply {
            action = ACTION_STOP_SESSION
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val speedText = "${telemetry.formattedSpeed} ${telemetry.speedUnit.label}"
        val radarInfo = _nextRadarNotice.value.ifBlank { "Nenhum radar próximo" }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WidVelo: $speedText")
            .setContentText(radarInfo)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Encerrar Sessão", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(telemetry: VehicleTelemetry) {
        if (_isServiceRunning.value) {
            try {
                notificationManager.notify(NOTIFICATION_ID, buildNotification(telemetry))
            } catch (_: Exception) {}
        }
    }

    companion object {
        const val CHANNEL_ID = "widvelo_location_channel"
        const val NOTIFICATION_ID = 4001

        const val ACTION_START_SESSION = "com.creativeam.widvelo.action.START_SESSION"
        const val ACTION_STOP_SESSION = "com.creativeam.widvelo.action.STOP_SESSION"

        private val _currentTelemetry = MutableStateFlow(VehicleTelemetry())
        val currentTelemetry: StateFlow<VehicleTelemetry> = _currentTelemetry.asStateFlow()

        private val _nextRadarNotice = MutableStateFlow("")
        val nextRadarNotice: StateFlow<String> = _nextRadarNotice.asStateFlow()

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private var simulationJob: kotlinx.coroutines.Job? = null
        private val _isSimulationRunning = MutableStateFlow(false)
        val isSimulationRunning: StateFlow<Boolean> = _isSimulationRunning.asStateFlow()

        fun startSimulation(scope: CoroutineScope) {
            if (_isSimulationRunning.value) return
            _isSimulationRunning.value = true
            simulationJob?.cancel()
            simulationJob = scope.launch(Dispatchers.Default) {
                // Etapas de rota simulada em São Bernardo do Campo (Rua dos Vianas)
                val steps = listOf(
                    // 1. Condução normal em SBC a 44 km/h (sem alertas)
                    VehicleTelemetry(speedKmh = 44.0, speedDisplay = 44, bearing = 140f, latitude = -23.6852, longitude = -46.5415, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true),
                    VehicleTelemetry(speedKmh = 46.0, speedDisplay = 46, bearing = 140f, latitude = -23.6848, longitude = -46.5435, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true),
                    // 2. Parada no Semáforo da Pereira Barreto (0 km/h)
                    VehicleTelemetry(speedKmh = 0.0, speedDisplay = 0, bearing = 140f, latitude = -23.6845, longitude = -46.5450, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true),
                    VehicleTelemetry(speedKmh = 0.0, speedDisplay = 0, bearing = 140f, latitude = -23.6845, longitude = -46.5450, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true),
                    // 3. Farol abre, aceleração
                    VehicleTelemetry(speedKmh = 32.0, speedDisplay = 32, bearing = 140f, latitude = -23.6838, longitude = -46.5410, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true),
                    VehicleTelemetry(speedKmh = 48.0, speedDisplay = 48, bearing = 140f, latitude = -23.6834, longitude = -46.5385, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true),
                    // 4. Excesso de velocidade (58 km/h em via de 50) + Radar SBC-VIA-01 à frente (350m)
                    VehicleTelemetry(speedKmh = 58.0, speedDisplay = 58, bearing = 140f, latitude = -23.6828, longitude = -46.5365, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true),
                    VehicleTelemetry(speedKmh = 59.0, speedDisplay = 59, bearing = 140f, latitude = -23.6823, longitude = -46.5355, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true),
                    // 5. Radar iminente (120m)
                    VehicleTelemetry(speedKmh = 57.0, speedDisplay = 57, bearing = 140f, latitude = -23.6821, longitude = -46.5352, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true),
                    // 6. Redução após o radar (46 km/h - normalizado)
                    VehicleTelemetry(speedKmh = 46.0, speedDisplay = 46, bearing = 140f, latitude = -23.6815, longitude = -46.5345, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true),
                    // 7. Obras na pista à frente (SBC-INC-01 a 250m)
                    VehicleTelemetry(speedKmh = 38.0, speedDisplay = 38, bearing = 140f, latitude = -23.6808, longitude = -46.5338, accuracyMeters = 3f, signalQuality = GpsSignalQuality.EXCELLENT, isSessionActive = true)
                )

                var index = 0
                while (_isSimulationRunning.value) {
                    val tel = steps[index % steps.size]
                    _currentTelemetry.value = tel
                    kotlinx.coroutines.delay(2400L)
                    index++
                }
            }
        }

        fun stopSimulation() {
            _isSimulationRunning.value = false
            simulationJob?.cancel()
            simulationJob = null
        }

        fun updateRadarNotice(notice: String) {
            _nextRadarNotice.value = notice
        }

        fun start(context: Context) {
            val intent = Intent(context, WidVeloLocationService::class.java).apply {
                action = ACTION_START_SESSION
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, WidVeloLocationService::class.java).apply {
                action = ACTION_STOP_SESSION
            }
            context.startService(intent)
        }
    }
}
