package com.creativeam.widvelo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeam.widvelo.location.VehicleTelemetry
import com.creativeam.widvelo.ui.theme.CyanAccent
import com.creativeam.widvelo.ui.theme.ElectricBlue
import com.creativeam.widvelo.ui.theme.SafeGreen
import com.creativeam.widvelo.ui.theme.SlateGray

data class AuditLogEntry(
    val checkpointId: String,
    val title: String,
    val date: String,
    val version: String,
    val description: String,
    val filesAffected: List<String>,
    val status: String
)

val OFFICIAL_AUDIT_LOG = listOf(
    AuditLogEntry(
        checkpointId = "#001",
        title = "Fundação Arquitetural, Repositório & CI Automatizado",
        date = "23/10/2026",
        version = "v1.0.0 (Build 1)",
        description = "Configuração do Gradle 8.2.2, Kotlin 1.9.22, KSP, Material 3, Keystore fixa de release e esteira de CI no GitHub Actions.",
        filesAffected = listOf("build.gradle", "settings.gradle", "build-apk.yml", "widvelo-fixed.keystore"),
        status = "CONCLUÍDO"
    ),
    AuditLogEntry(
        checkpointId = "#002",
        title = "Motor de Telemetria GPS & Filtro Anti-Oscilação",
        date = "23/10/2026",
        version = "v1.0.0 (Build 1)",
        description = "Implementação do filtro de velocidade passa-baixa (EMA) com Trava de Imobilidade (Zero-Lock a 1.5 km/h) e FusedLocationProvider com fallback offline.",
        filesAffected = listOf("GpsSpeedFilter.kt", "LocationTracker.kt", "GpsSignalQuality.kt", "WidVeloLocationService.kt"),
        status = "CONCLUÍDO"
    ),
    AuditLogEntry(
        checkpointId = "#003",
        title = "Banco Room de Radares & Filtro Angular de Sentido",
        date = "23/10/2026",
        version = "v1.0.0 (Build 1)",
        description = "Persistência Room de radares oficiais ANTT, DER-SP e OSM. Cálculo de distância Haversine e filtro de azimute (±35°) para eliminar pistas contrárias.",
        filesAffected = listOf("RadarEntity.kt", "RadarDao.kt", "RadarDetector.kt", "PreloadedRadars.kt", "WidVeloDatabase.kt"),
        status = "CONCLUÍDO"
    ),
    AuditLogEntry(
        checkpointId = "#004",
        title = "Reconhecimento de Limites de Via & Alertas de Excesso",
        date = "23/10/2026",
        version = "v1.0.0 (Build 1)",
        description = "Detecção de limite de velocidade com regra estrita de não inventar dados. Avaliação de tolerância (+0, +3, +5 km/h) e emissão de beeps nativos sem delay.",
        filesAffected = listOf("RoadSpeedLimit.kt", "SpeedLimitEngine.kt", "OverspeedEvaluator.kt", "AudioAlerter.kt"),
        status = "CONCLUÍDO"
    ),
    AuditLogEntry(
        checkpointId = "#005",
        title = "Widget Flutuante Arrastável sobre Waze e Maps",
        date = "23/10/2026",
        version = "v1.0.0 (Build 1)",
        description = "Janela flutuante WindowManager compacta e arrastável com memória de coordenadas, velocidade em destaque e indicador de radar e limite da via.",
        filesAffected = listOf("FloatingWidVeloWidget.kt", "FloatingWidVeloService.kt", "FloatingWidgetState.kt"),
        status = "CONCLUÍDO"
    ),
    AuditLogEntry(
        checkpointId = "#006",
        title = "Manifest Android, Application Class, Recursos XML & Suíte de Testes",
        date = "23/10/2026",
        version = "v1.0.0 (Build 1)",
        description = "Configuração do AndroidManifest com permissões rigorosas de GPS e Overlay, classe WidVeloApplication, recursos XML de strings/cores/temas e testes unitários automatizados.",
        filesAffected = listOf("AndroidManifest.xml", "WidVeloApplication.kt", "strings.xml", "colors.xml", "themes.xml", "GpsSpeedFilterTest.kt", "RadarDetectorTest.kt", "OverspeedEvaluatorTest.kt"),
        status = "CONCLUÍDO"
    ),
    AuditLogEntry(
        checkpointId = "#007",
        title = "Detector de Semáforos Urbanos & Temporizador de Ciclo de Parada",
        date = "23/10/2026",
        version = "v1.0.0 (Build 1)",
        description = "Integração da base Room com semáforos urbanos (OSM/CET), filtro angular de cruzamento, detecção de imobilização no farol e temporizador progressivo no mostrador e no Widget Flutuante.",
        filesAffected = listOf("TrafficLightEntity.kt", "TrafficLightDao.kt", "PreloadedTrafficLights.kt", "TrafficLightAlert.kt", "TrafficLightDetector.kt", "WidVeloDatabase.kt", "FloatingWidVeloWidget.kt", "FloatingWidVeloService.kt", "MainSpeedometerScreen.kt", "MainActivity.kt", "TrafficLightDetectorTest.kt"),
        status = "CONCLUÍDO"
    ),
    AuditLogEntry(
        checkpointId = "#008",
        title = "Widget Redondo Waze, Radares de SBC & Refinamento Visual",
        date = "23/10/2026",
        version = "v1.0.1 (Build 2)",
        description = "Redesign completo do Widget Flutuante no estilo circular Waze: velocímetro redondo branco com transição para vermelho em excesso, placa R-19 circular que expande e fica maiorzinha, botão de fechar (✕), motor de limite urbano padrão e banco de radares de SBC/Grande ABC populado com 35+ pontos oficiais.",
        filesAffected = listOf("FloatingWidVeloWidget.kt", "FloatingWidVeloService.kt", "PreloadedRadars.kt", "PreloadedTrafficLights.kt", "SpeedLimitEngine.kt", "WidVeloDatabase.kt", "MainSpeedometerScreen.kt", "RadarListScreen.kt", "WidVeloSettingsScreen.kt", "build.gradle"),
        status = "CONCLUÍDO"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidVeloAdminScreen(
    telemetry: VehicleTelemetry,
    radarCount: Int,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Painel de Auditoria & ADM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Livro de Registros & Diagnóstico em Tempo Real",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Cartão de Diagnóstico de Hardware e Sensores
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = ElectricBlue)
                            Text(text = "Diagnóstico do Sensor GPS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Velocidade Filtrada:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text("${telemetry.speedDisplay} km/h (real: ${String.format(java.util.Locale.US, "%.1f", telemetry.speedKmh)})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rumo / Azimute (Bearing):", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text("${String.format(java.util.Locale.US, "%.1f", telemetry.bearing)}°", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Precisão do Satélite:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text("± ${String.format(java.util.Locale.US, "%.1f", telemetry.accuracyMeters)}m (${telemetry.signalQuality.label})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Radares Carregados no Banco:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text("$radarCount pontos homologados", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SafeGreen)
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CyanAccent)
                    Text(
                        text = "Histórico de Modificações & Pontos de Restauração",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            items(OFFICIAL_AUDIT_LOG) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElectricBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = entry.checkpointId,
                                    color = ElectricBlue,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = entry.date,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Text(
                            text = entry.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = entry.description,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 17.sp
                        )

                        Text(
                            text = "Arquivos: ${entry.filesAffected.joinToString(", ")}",
                            fontSize = 11.sp,
                            color = SlateGray,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Status: ${entry.status}",
                                color = SafeGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
