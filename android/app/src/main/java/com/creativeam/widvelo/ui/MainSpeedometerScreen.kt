package com.creativeam.widvelo.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeam.widvelo.R
import com.creativeam.widvelo.incident.RoadIncidentAlert
import com.creativeam.widvelo.location.VehicleTelemetry
import com.creativeam.widvelo.overspeed.OverspeedLevel
import com.creativeam.widvelo.radar.RadarAlert
import com.creativeam.widvelo.speedlimit.RoadSpeedLimit
import com.creativeam.widvelo.trafficlight.TrafficLightAlert
import com.creativeam.widvelo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSpeedometerScreen(
    telemetry: VehicleTelemetry,
    speedLimit: RoadSpeedLimit,
    activeRadarAlert: RadarAlert?,
    activeTrafficLightAlert: TrafficLightAlert? = null,
    activeIncidentAlert: RoadIncidentAlert? = null,
    overspeedLevel: OverspeedLevel,
    isWidgetActive: Boolean,
    isSimulationRunning: Boolean = false,
    onToggleSimulation: () -> Unit = {},
    onToggleSession: () -> Unit,
    onToggleWidget: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRadarList: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    var logoTapCount by remember { mutableStateOf(0) }

    val ringColor by animateColorAsState(
        targetValue = when (overspeedLevel) {
            OverspeedLevel.EXCEEDED -> AlertRed
            OverspeedLevel.CAUTION -> CautionAmber
            OverspeedLevel.NORMAL -> SafeGreen
            OverspeedLevel.NO_LIMIT -> ElectricBlue
        },
        label = "SpeedometerRingColor"
    )

    val isOverspeed = overspeedLevel == OverspeedLevel.EXCEEDED || overspeedLevel == OverspeedLevel.CAUTION

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.clickable {
                            logoTapCount++
                            if (logoTapCount >= 3) {
                                logoTapCount = 0
                                onOpenAdmin()
                            }
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_speedometer),
                            contentDescription = "WidVelo Logo",
                            modifier = Modifier.size(34.dp)
                        )
                        Column {
                            Text(
                                text = "WidVelo",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "Velocímetro GPS & Radares",
                                fontSize = 11.sp,
                                color = SlateGray,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAdmin) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Auditoria & ADM", tint = ElectricBlue)
                    }
                    IconButton(onClick = onOpenRadarList) {
                        Icon(imageVector = Icons.Default.Radar, contentDescription = "Radares", tint = SlateGray)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Configurações", tint = SlateGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Linha Superior: Chip de Sinal GPS + Botão de Demonstração / Teste de Alertas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(android.graphics.Color.parseColor(telemetry.signalQuality.colorHex)).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(android.graphics.Color.parseColor(telemetry.signalQuality.colorHex)).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(telemetry.signalQuality.colorHex)))
                        )
                        Text(
                            text = if (isSimulationRunning) "SIMULAÇÃO SBC" else "${telemetry.signalQuality.label} (±${telemetry.accuracyMeters.toInt()}m)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulationRunning) CautionAmber else Color(android.graphics.Color.parseColor(telemetry.signalQuality.colorHex)),
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    onClick = onToggleSimulation,
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSimulationRunning) AlertRed.copy(alpha = 0.14f) else ElectricBlue.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (isSimulationRunning) AlertRed else ElectricBlue.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isSimulationRunning) "⏹ Parar Demo" else "🧪 Testar Alertas",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulationRunning) AlertRed else ElectricBlue,
                            maxLines = 1
                        )
                    }
                }
            }

            // 2. Velocímetro Digital Circular Central
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(if (isOverspeed) AlertRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                    .border(BorderStroke(8.dp, ringColor.copy(alpha = 0.9f)), CircleShape)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = telemetry.formattedSpeed,
                        fontSize = 70.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isOverspeed) AlertRed else MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-2).sp,
                        maxLines = 1
                    )
                    Text(
                        text = telemetry.speedUnit.label.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverspeed) AlertRed else SlateGray,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )

                    if (telemetry.bearing > 0f && telemetry.speedKmh >= 5.0) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = SlateGray.copy(alpha = 0.12f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "${telemetry.bearing.toInt()}° Rumo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateGray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 3. Painel de Informações da Via (Limite + Incidentes + Radar + Semáforo)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card de Limite Regulamentar (Placa R-19 Waze Style)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isOverspeed) AlertRed.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LIMITE REGULAMENTAR",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray,
                                letterSpacing = 0.5.sp,
                                maxLines = 1
                            )
                            Text(
                                text = speedLimit.roadName ?: "Via Urbana Regulamentada",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Placa Circular R-19 (expande em overspeed)
                        val limitVal = speedLimit.limitKmh ?: 50
                        Box(
                            modifier = Modifier
                                .size(if (isOverspeed) 48.dp else 42.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(BorderStroke(if (isOverspeed) 4.dp else 3.dp, AlertRed), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$limitVal",
                                fontWeight = FontWeight.Black,
                                fontSize = if (isOverspeed) 17.sp else 15.sp,
                                color = if (isOverspeed) AlertRed else Graphite,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Card de Alerta de Via Interditada / Obras (quando presente)
                if (activeIncidentAlert != null) {
                    val alertColor = Color(android.graphics.Color.parseColor(activeIncidentAlert.entity.type.colorHex))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = alertColor.copy(alpha = 0.10f)),
                        border = BorderStroke(1.5.dp, alertColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(alertColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeIncidentAlert.entity.type.badge,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeIncidentAlert.entity.type.label.uppercase(),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = alertColor,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${activeIncidentAlert.entity.roadName}: ${activeIncidentAlert.entity.description}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = activeIncidentAlert.formattedDistance,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = alertColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Card de Fiscalização Eletrônica / Radar Próximo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeRadarAlert != null && activeRadarAlert.isImminent) {
                            AlertRed.copy(alpha = 0.08f)
                        } else if (activeRadarAlert != null) {
                            CautionAmber.copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (activeRadarAlert != null) 1.5.dp else 1.dp,
                        color = if (activeRadarAlert != null && activeRadarAlert.isImminent) {
                            AlertRed
                        } else if (activeRadarAlert != null) {
                            CautionAmber
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (activeRadarAlert != null) CautionAmber.copy(alpha = 0.15f)
                                    else SlateGray.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (activeRadarAlert != null) CautionAmber else SlateGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (activeRadarAlert != null) "⚠️ RADAR HOMOLOGADO À FRENTE" else "FISCALIZAÇÃO ELETRÔNICA",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeRadarAlert != null) CautionAmber else SlateGray,
                                maxLines = 1
                            )
                            Text(
                                text = activeRadarAlert?.radar?.highway ?: "Nenhum radar nos próximos 1.500m",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (activeRadarAlert != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeRadarAlert.formattedDistance,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = if (activeRadarAlert.isImminent) AlertRed else ElectricBlue,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Card de Semáforo / Cruzamento (quando ativo)
                if (activeTrafficLightAlert != null) {
                    val isWaiting = activeTrafficLightAlert.isAtStandstill
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWaiting) AlertRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isWaiting) AlertRed else SafeGreen.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isWaiting) AlertRed.copy(alpha = 0.15f)
                                            else SafeGreen.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Traffic,
                                        contentDescription = null,
                                        tint = if (isWaiting) AlertRed else SafeGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isWaiting) "🚦 PARADO NO FAROL • AGUARDE" else "🚦 SEMÁFORO À FRENTE",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isWaiting) AlertRed else SafeGreen,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${activeTrafficLightAlert.entity.roadName} x ${activeTrafficLightAlert.entity.crossRoadName}",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = if (isWaiting) "~${activeTrafficLightAlert.estimatedRemainingSeconds}s" else activeTrafficLightAlert.formattedDistance,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isWaiting) AlertRed else SafeGreen,
                                    maxLines = 1
                                )
                            }

                            if (isWaiting && activeTrafficLightAlert.progressFraction > 0f) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { activeTrafficLightAlert.progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = AlertRed,
                                    trackColor = AlertRed.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Painel Inferior de Ações (Widget Flutuante + Iniciar/Pausar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onToggleWidget,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, if (isWidgetActive) ElectricBlue else SlateGray.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isWidgetActive) Icons.Default.LayersClear else Icons.Default.Layers,
                        contentDescription = null,
                        tint = if (isWidgetActive) ElectricBlue else SlateGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isWidgetActive) "Ocultar Widget" else "Widget Flutuante",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isWidgetActive) ElectricBlue else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onToggleSession,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (telemetry.isSessionActive) AlertRed else ElectricBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = if (telemetry.isSessionActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (telemetry.isSessionActive) "Parar GPS" else "Iniciar GPS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
