package com.creativeam.widvelo.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeam.widvelo.R
import com.creativeam.widvelo.location.VehicleTelemetry
import com.creativeam.widvelo.overspeed.OverspeedLevel
import com.creativeam.widvelo.radar.RadarAlert
import com.creativeam.widvelo.speedlimit.RoadSpeedLimit
import com.creativeam.widvelo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSpeedometerScreen(
    telemetry: VehicleTelemetry,
    speedLimit: RoadSpeedLimit,
    activeRadarAlert: RadarAlert?,
    overspeedLevel: OverspeedLevel,
    isWidgetActive: Boolean,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Velocímetro GPS & Radares",
                                fontSize = 10.5.sp,
                                color = SlateGray
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
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Chip de Qualidade do GPS e Satélites
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(android.graphics.Color.parseColor(telemetry.signalQuality.colorHex)).copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color(android.graphics.Color.parseColor(telemetry.signalQuality.colorHex)).copy(alpha = 0.4f)),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
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
                        text = "${telemetry.signalQuality.label} (±${telemetry.accuracyMeters.toInt()}m)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(telemetry.signalQuality.colorHex))
                    )
                }
            }

            // 2. Velocímetro Digital Gigante Central
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(BorderStroke(8.dp, ringColor.copy(alpha = 0.85f)), CircleShape)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = telemetry.formattedSpeed,
                        fontSize = 78.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        text = telemetry.speedUnit.label.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray,
                        letterSpacing = 1.sp
                    )

                    if (telemetry.bearing > 0f && telemetry.speedKmh >= 5.0) {
                        Text(
                            text = "${telemetry.bearing.toInt()}° Rumo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateGray.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // 3. Limite da Via e Próximo Radar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Linha de Limite de Velocidade
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "LIMITE DA VIA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray
                            )
                            Text(
                                text = speedLimit.roadName ?: "Via sem identificação",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (speedLimit.isAvailable) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(BorderStroke(4.dp, AlertRed), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${speedLimit.limitKmh}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Graphite
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SlateGray.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = speedLimit.displayText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGray,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Card de Radar Próximo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeRadarAlert != null && activeRadarAlert.isImminent) {
                            CautionAmber.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (activeRadarAlert != null) 2.dp else 1.dp,
                        color = if (activeRadarAlert != null && activeRadarAlert.isImminent) CautionAmber else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (activeRadarAlert != null) CautionAmber else SlateGray
                            )
                            Column {
                                Text(
                                    text = if (activeRadarAlert != null) "RADAR CONFIRMADO À FRENTE" else "FISCALIZAÇÃO ELETRÔNICA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeRadarAlert != null) CautionAmber else SlateGray
                                )
                                Text(
                                    text = activeRadarAlert?.radar?.highway ?: "Nenhum radar próximo em rota",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (activeRadarAlert != null) {
                            Text(
                                text = activeRadarAlert.formattedDistance,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (activeRadarAlert.isImminent) AlertRed else ElectricBlue
                            )
                        }
                    }
                }
            }

            // 4. Painel Inferior de Controles (Widget Flutuante + Iniciar/Parar Sessão)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão de alternar Widget Flutuante
                OutlinedButton(
                    onClick = onToggleWidget,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, if (isWidgetActive) ElectricBlue else SlateGray.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isWidgetActive) Icons.Default.LayersClear else Icons.Default.Layers,
                        contentDescription = null,
                        tint = if (isWidgetActive) ElectricBlue else SlateGray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isWidgetActive) "Ocultar Widget" else "Widget Flutuante",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = if (isWidgetActive) ElectricBlue else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Botão de Iniciar / Encerrar Sessão
                Button(
                    onClick = onToggleSession,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (telemetry.isSessionActive) AlertRed else ElectricBlue
                    )
                ) {
                    Icon(
                        imageVector = if (telemetry.isSessionActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (telemetry.isSessionActive) "Parar" else "Iniciar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
