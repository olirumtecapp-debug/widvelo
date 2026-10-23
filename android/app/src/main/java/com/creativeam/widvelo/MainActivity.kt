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
import com.creativeam.widvelo.incident.RoadIncidentAlert
import com.creativeam.widvelo.incident.RoadIncidentDetector
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
import com.creativeam.widvelo.sync.CitySyncManager
import com.creativeam.widvelo.trafficlight.TrafficLightAlert
import com.creativeam.widvelo.trafficlight.TrafficLightDetector
import com.creativeam.widvelo.ui.MainSpeedometerScreen
import com.creativeam.widvelo.ui.RadarListScreen
import com.creativeam.widvelo.ui.WidVeloAdminScreen
import com.creativeam.widvelo.ui.WidVeloSettingsScreen
import com.creativeam.widvelo.ui.theme.WidVeloTheme
import com.creativeam.widvelo.widget.FloatingWidVeloService
import com.creativeam.widvelo.update.AppUpdateInfo
import com.creativeam.widvelo.update.AppUpdateManager
import com.creativeam.widvelo.update.UpdateDialog
import com.creativeam.widvelo.update.UpdateDownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var database: WidVeloDatabase
    private lateinit var speedLimitEngine: SpeedLimitEngine
    private lateinit var trafficLightDetector: TrafficLightDetector
    private lateinit var roadIncidentDetector: RoadIncidentDetector
    private lateinit var citySyncManager: CitySyncManager
    private lateinit var overspeedEvaluator: OverspeedEvaluator
    private lateinit var audioAlerter: AudioAlerter
    private val appUpdateManager = AppUpdateManager()

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
        roadIncidentDetector = RoadIncidentDetector(database.roadIncidentDao())
        citySyncManager = CitySyncManager(database)
        overspeedEvaluator = OverspeedEvaluator(toleranceKmh = 3)
        audioAlerter = AudioAlerter(applicationContext)

        // Solicita permissões de localização
        checkAndRequestPermissions()

        setContent {
            val scope = rememberCoroutineScope()
            var currentScreen by remember { mutableStateOf("main") }
            var currentUnit by remember { mutableStateOf(SpeedUnit.KMH) }
            var toleranceKmh by remember { mutableStateOf(3) }
            var soundEnabled by remember { mutableStateOf(true) }
            var darkThemeEnabled by remember { mutableStateOf(false) }

            val telemetry by WidVeloLocationService.currentTelemetry.collectAsState()
            val isWidgetActive by FloatingWidVeloService.isWidgetActive.collectAsState()

            var activeRadarAlert by remember { mutableStateOf<RadarAlert?>(null) }
            var activeTrafficLightAlert by remember { mutableStateOf<TrafficLightAlert?>(null) }
            var activeIncidentAlert by remember { mutableStateOf<RoadIncidentAlert?>(null) }
            var currentRoadLimit by remember { mutableStateOf(RoadSpeedLimit.UNAVAILABLE) }
            var allRadarsList by remember { mutableStateOf<List<RadarEntity>>(emptyList()) }

            // Carrega radares, semáforos e incidentes do banco e atualiza cálculo geográfico
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

                    activeIncidentAlert = withContext(Dispatchers.IO) {
                        roadIncidentDetector.evaluate(telemetry)
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
                // Diálogo de Atualização Automática OTA
                if (availableUpdate != null) {
                    UpdateDialog(
                        updateInfo = availableUpdate!!,
                        downloadState = updateDownloadState,
                        onStartDownload = {
                            scope.launch {
                                appUpdateManager.downloadAndInstallApk(
                                    context = this@MainActivity,
                                    apkUrl = availableUpdate!!.directDownloadUrl.ifBlank { availableUpdate!!.apkUrl }
                                ) { state ->
                                    updateDownloadState = state
                                }
                            }
                        },
                        onDismiss = {
                            availableUpdate = null
                            updateDownloadState = UpdateDownloadState.Idle
                        }
                    )
                }
                when (currentScreen) {
                    "main" -> {
                        MainSpeedometerScreen(
                            telemetry = telemetry,
                            speedLimit = currentRoadLimit,
                            activeRadarAlert = activeRadarAlert,
                            activeTrafficLightAlert = activeTrafficLightAlert,
                            activeIncidentAlert = activeIncidentAlert,
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
                            onCheckForUpdates = triggerUpdateCheck,
                            onCheckForUpdates = triggerUpdateCheck,
                            onBack = { currentScreen = "main" }
                        )
                    }
                    "radars" -> {
                        RadarListScreen(
                            radars = allRadarsList,
                            onCheckForUpdates = triggerUpdateCheck,
                            onBack = { currentScreen = "main" }
                        )
                    }
                    "admin" -> {
                        WidVeloAdminScreen(
                            telemetry = telemetry,
                            radarCount = allRadarsList.size,
                            onSyncTerritory = {
                                scope.launch {
                                    val res = citySyncManager.syncTerritory("São Bernardo do Campo")
                                    withContext(Dispatchers.IO) {
                                        allRadarsList = database.radarDao().getAllRadars()
                                    }
                                    Toast.makeText(this@MainActivity, res.message, Toast.LENGTH_LONG).show()
                                }
                            },
                            onCheckForUpdates = triggerUpdateCheck,
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
        } else {
            WidVeloLocationService.start(this)
        }
    }

    private fun toggleFloatingWidget(isActive: Boolean) {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Conceda permissão de sobreposição para exibir o velocímetro flutuante.", Toast.LENGTH_LONG).show()
            return
        }

        if (isActive) {
            FloatingWidVeloService.stop(this)
        } else {
            FloatingWidVeloService.start(this)
        }
    }
}
