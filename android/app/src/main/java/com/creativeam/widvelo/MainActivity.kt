package com.creativeam.widvelo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.creativeam.widvelo.location.SpeedUnit
import com.creativeam.widvelo.location.VehicleTelemetry
import com.creativeam.widvelo.location.WidVeloLocationService
import com.creativeam.widvelo.overspeed.AudioAlerter
import com.creativeam.widvelo.overspeed.OverspeedEvaluator
import com.creativeam.widvelo.overspeed.OverspeedLevel
import com.creativeam.widvelo.radar.RadarAlert
import com.creativeam.widvelo.radar.RadarDetector
import com.creativeam.widvelo.radar.RadarEntity
import com.creativeam.widvelo.radar.WidVeloDatabase
import com.creativeam.widvelo.speedlimit.RoadSpeedLimit
import com.creativeam.widvelo.speedlimit.SpeedLimitEngine
import com.creativeam.widvelo.trafficlight.TrafficLightAlert
import com.creativeam.widvelo.trafficlight.TrafficLightDetector
import com.creativeam.widvelo.ui.MainSpeedometerScreen
import com.creativeam.widvelo.ui.RadarListScreen
import com.creativeam.widvelo.ui.WidVeloAdminScreen
import com.creativeam.widvelo.ui.WidVeloSettingsScreen
import com.creativeam.widvelo.ui.theme.WidVeloTheme
import com.creativeam.widvelo.widget.FloatingWidVeloService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var database: WidVeloDatabase
    private lateinit var speedLimitEngine: SpeedLimitEngine
    private lateinit var trafficLightDetector: TrafficLightDetector
    private lateinit var overspeedEvaluator: OverspeedEvaluator
    private lateinit var audioAlerter: AudioAlerter

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (fineLocationGranted) {
            WidVeloLocationService.start(this)
        } else {
            Toast.makeText(this, "Permissão de GPS necessária para o velocímetro funcionar.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = WidVeloDatabase.getInstance(applicationContext)
        speedLimitEngine = SpeedLimitEngine(database.radarDao())
        trafficLightDetector = TrafficLightDetector(database.trafficLightDao())
        overspeedEvaluator = OverspeedEvaluator(toleranceKmh = 3)
        audioAlerter = AudioAlerter(applicationContext)

        // Solicita permissões de localização
        checkAndRequestPermissions()

        setContent {
            var currentScreen by remember { mutableStateOf("main") }
            var currentUnit by remember { mutableStateOf(SpeedUnit.KMH) }
            var toleranceKmh by remember { mutableStateOf(3) }
            var soundEnabled by remember { mutableStateOf(true) }
            var darkThemeEnabled by remember { mutableStateOf(false) }

            val telemetry by WidVeloLocationService.currentTelemetry.collectAsState()
            val isWidgetActive by FloatingWidVeloService.isWidgetActive.collectAsState()

            var activeRadarAlert by remember { mutableStateOf<RadarAlert?>(null) }
            var activeTrafficLightAlert by remember { mutableStateOf<TrafficLightAlert?>(null) }
            var currentRoadLimit by remember { mutableStateOf(RoadSpeedLimit.UNAVAILABLE) }
            var allRadarsList by remember { mutableStateOf<List<RadarEntity>>(emptyList()) }

            // Carrega radares e semáforos do banco e atualiza cálculo geográfico
            LaunchedEffect(telemetry) {
                if (telemetry.isSessionActive) {
                    val dao = database.radarDao()
                    val nearbyRadars = withContext(Dispatchers.IO) {
                        dao.getRadarsInBoundingBox(
                            minLat = telemetry.latitude - 0.02,
                            maxLat = telemetry.latitude + 0.02,
                            minLon = telemetry.longitude - 0.02,
                            maxLon = telemetry.longitude + 0.02
                        )
                    }

                    activeRadarAlert = nearbyRadars
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

                    activeTrafficLightAlert = withContext(Dispatchers.IO) {
                        trafficLightDetector.evaluate(telemetry)
                    }

                    currentRoadLimit = withContext(Dispatchers.IO) {
                        speedLimitEngine.resolveSpeedLimit(
                            latitude = telemetry.latitude,
                            longitude = telemetry.longitude,
                            currentSpeedKmh = telemetry.speedKmh,
                            activeRadarAlert = activeRadarAlert
                        )
                    }
                }
            }

            // Atualiza lista completa para a tela de radares e garante semente carregada
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    WidVeloDatabase.ensureSeeded(database)
                    allRadarsList = database.radarDao().getAllRadars()
                }
            }

            val overspeedResult = remember(telemetry.speedKmh, currentRoadLimit, toleranceKmh) {
                overspeedEvaluator.setToleranceKmh(toleranceKmh)
                val res = overspeedEvaluator.evaluate(telemetry.speedKmh, currentRoadLimit)
                if (soundEnabled && res.shouldTriggerSound) {
                    audioAlerter.playOverspeedAlert()
                }
                res
            }

            WidVeloTheme(darkTheme = darkThemeEnabled) {
                when (currentScreen) {
                    "main" -> {
                        MainSpeedometerScreen(
                            telemetry = telemetry,
                            speedLimit = currentRoadLimit,
                            activeRadarAlert = activeRadarAlert,
                            activeTrafficLightAlert = activeTrafficLightAlert,
                            overspeedLevel = overspeedResult.level,
                            isWidgetActive = isWidgetActive,
                            onToggleSession = {
                                if (telemetry.isSessionActive) {
                                    WidVeloLocationService.stop(this@MainActivity)
                                } else {
                                    WidVeloLocationService.start(this@MainActivity)
                                }
                            },
                            onToggleWidget = {
                                toggleFloatingWidget(isWidgetActive)
                            },
                            onOpenSettings = { currentScreen = "settings" },
                            onOpenRadarList = { currentScreen = "radars" },
                            onOpenAdmin = { currentScreen = "admin" }
                        )
                    }
                    "settings" -> {
                        WidVeloSettingsScreen(
                            currentUnit = currentUnit,
                            toleranceKmh = toleranceKmh,
                            soundEnabled = soundEnabled,
                            darkThemeEnabled = darkThemeEnabled,
                            onUnitChanged = { currentUnit = it },
                            onToleranceChanged = { toleranceKmh = it },
                            onSoundChanged = { soundEnabled = it },
                            onThemeChanged = { darkThemeEnabled = it },
                            onBack = { currentScreen = "main" }
                        )
                    }
                    "radars" -> {
                        RadarListScreen(
                            radars = allRadarsList,
                            onBack = { currentScreen = "main" }
                        )
                    }
                    "admin" -> {
                        WidVeloAdminScreen(
                            telemetry = telemetry,
                            radarCount = allRadarsList.size,
                            onBack = { currentScreen = "main" }
                        )
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun toggleFloatingWidget(currentlyActive: Boolean) {
        if (currentlyActive) {
            FloatingWidVeloService.stop(this)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Autorize a sobreposição para ver o velocímetro sobre o Waze/Maps", Toast.LENGTH_LONG).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                FloatingWidVeloService.start(this)
            }
        }
    }

    override fun onDestroy() {
        audioAlerter.release()
        super.onDestroy()
    }
}
