package com.creativeam.widvelo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeam.widvelo.radar.RadarEntity
import com.creativeam.widvelo.ui.theme.AlertRed
import com.creativeam.widvelo.ui.theme.ElectricBlue
import com.creativeam.widvelo.ui.theme.SlateGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarListScreen(
    radars: List<RadarEntity>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Base de Radares do Brasil", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${radars.size} radares homologados (ANTT, DER e OSM)", fontSize = 12.sp, color = SlateGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(radars) { radar ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = ElectricBlue
                                )
                            }
                            Column {
                                Text(
                                    text = radar.highway ?: "Rodovia não especificada",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${radar.municipality ?: ""} - ${radar.state ?: ""} (${radar.kmReference ?: ""})",
                                    fontSize = 12.sp,
                                    color = SlateGray
                                )
                                Text(
                                    text = "Fonte: ${radar.source} · Sentido: ${radar.bearing?.toInt() ?: 0}°",
                                    fontSize = 11.sp,
                                    color = ElectricBlue
                                )
                            }
                        }

                        if (radar.speedLimit != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AlertRed.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed)
                            ) {
                                Text(
                                    text = "${radar.speedLimit} km/h",
                                    color = AlertRed,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
