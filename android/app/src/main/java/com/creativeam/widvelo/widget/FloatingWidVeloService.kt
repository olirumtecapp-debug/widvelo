package com.creativeam.widvelo.widget

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.creativeam.widvelo.location.WidVeloLocationService
import com.creativeam.widvelo.overspeed.AudioAlerter
import com.creativeam.widvelo.overspeed.OverspeedEvaluator
import com.creativeam.widvelo.overspeed.OverspeedLevel
import com.creativeam.widvelo.radar.RadarDetector
import com.creativeam.widvelo.radar.WidVeloDatabase
import com.creativeam.widvelo.speedlimit.SpeedLimitEngine
import com.creativeam.widvelo.trafficlight.TrafficLightDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Serviço que gerencia o ciclo de vida e a telemetria do Widget Flutuante sobreposto.
 */
class FloatingWidVeloService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var widget: FloatingWidVeloWidget? = null
    private lateinit var speedLimitEngine: SpeedLimitEngine
    private lateinit var trafficLightDetector: TrafficLightDetector
    private lateinit var overspeedEvaluator: OverspeedEvaluator
    private lateinit var audioAlerter: AudioAlerter

    override fun onCreate() {
        super.onCreate()
        val db = WidVeloDatabase.getInstance(applicationContext)
        speedLimitEngine = SpeedLimitEngine(db.radarDao())
        trafficLightDetector = TrafficLightDetector(db.trafficLightDao())
        overspeedEvaluator = OverspeedEvaluator(toleranceKmh = 3)
        audioAlerter = AudioAlerter(applicationContext)
        widget = FloatingWidVeloWidget(applicationContext)

        widget?.show()
        _isWidgetActive.value = true

        // Observa a telemetria do GPS emitida pelo serviço principal
        serviceScope.launch {
            WidVeloLocationService.currentTelemetry.collect { telemetry ->
                if (!telemetry.isSessionActive) return@collect

                val dao = db.radarDao()
                val nearbyRadars = withContext(Dispatchers.IO) {
                    dao.getRadarsInBoundingBox(
                        minLat = telemetry.latitude - 0.02,
                        maxLat = telemetry.latitude + 0.02,
                        minLon = telemetry.longitude - 0.02,
                        maxLon = telemetry.longitude + 0.02
                    )
                }

                // Avalia radares à frente no mesmo sentido
                val radarAlert = nearbyRadars
                    .mapNotNull {
                        RadarDetector.evaluateRadar(
                            vehicleLat = telemetry.latitude,
                            vehicleLon = telemetry.longitude,
                            vehicleSpeedKmh = telemetry.speedKmh,
                            vehicleBearing = telemetry.bearing,
                            radar = it
                        )
                    }
                    .minByOrNull { it.distanceMeters }

                // Avalia semáforos e temporizador de cruzamento à frente
                val trafficLightAlert = withContext(Dispatchers.IO) {
                    trafficLightDetector.evaluate(telemetry)
                }

                // Resolve limite de velocidade da via
                val roadLimit = withContext(Dispatchers.IO) {
                    speedLimitEngine.resolveSpeedLimit(
                        latitude = telemetry.latitude,
                        longitude = telemetry.longitude,
                        currentSpeedKmh = telemetry.speedKmh,
                        activeRadarAlert = radarAlert
                    )
                }

                // Avalia excesso de velocidade
                val overspeed = overspeedEvaluator.evaluate(telemetry.speedKmh, roadLimit)
                if (overspeed.shouldTriggerSound) {
                    audioAlerter.playOverspeedAlert()
                }

                // Se houver radar novo ou iminente, emite beep discreto
                if (radarAlert != null && radarAlert.isImminent) {
                    audioAlerter.playRadarBeep()
                }

                // Atualiza aviso na notificação do sistema
                if (radarAlert != null) {
                    WidVeloLocationService.updateRadarNotice("⚠️ ${radarAlert.radar.type.shortBadge} ${radarAlert.formattedDistance} (${radarAlert.radar.speedLimit ?: "--"} km/h)")
                } else if (trafficLightAlert != null && trafficLightAlert.isAtStandstill) {
                    WidVeloLocationService.updateRadarNotice("🚦 Farol: aguarde ~${trafficLightAlert.estimatedRemainingSeconds}s")
                } else if (roadLimit.isAvailable) {
                    WidVeloLocationService.updateRadarNotice("Limite da via: ${roadLimit.limitKmh} km/h")
                } else {
                    WidVeloLocationService.updateRadarNotice("Nenhum radar próximo")
                }

                // Atualiza o Widget Flutuante na tela
                widget?.updateState(
                    FloatingWidgetState(
                        speedValue = telemetry.formattedSpeed,
                        speedUnit = telemetry.speedUnit.label,
                        speedLimitText = if (roadLimit.isAvailable) "${roadLimit.limitKmh}" else null,
                        radarDistanceText = radarAlert?.formattedDistance,
                        radarTypeLabel = radarAlert?.radar?.type?.shortBadge,
                        trafficLightNotice = trafficLightAlert?.statusText,
                        isAtRedLightStandstill = trafficLightAlert?.isAtStandstill ?: false,
                        trafficLightProgress = trafficLightAlert?.progressFraction ?: 0f,
                        overspeedLevel = overspeed.level,
                        isRadarImminent = radarAlert?.isImminent ?: false,
                        gpsSignalQuality = telemetry.signalQuality,
                        isSessionActive = telemetry.isSessionActive
                    )
                )
            }
        }
    }

    override fun onDestroy() {
        widget?.hide()
        widget = null
        audioAlerter.release()
        _isWidgetActive.value = false
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private val _isWidgetActive = MutableStateFlow(false)
        val isWidgetActive: StateFlow<Boolean> = _isWidgetActive.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, FloatingWidVeloService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingWidVeloService::class.java)
            context.stopService(intent)
        }
    }
}
