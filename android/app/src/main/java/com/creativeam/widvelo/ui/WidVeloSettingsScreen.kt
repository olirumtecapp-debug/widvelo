package com.creativeam.widvelo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeam.widvelo.location.SpeedUnit
import com.creativeam.widvelo.ui.theme.ElectricBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidVeloSettingsScreen(
    currentUnit: SpeedUnit,
    toleranceKmh: Int,
    soundEnabled: Boolean,
    darkThemeEnabled: Boolean,
    onUnitChanged: (SpeedUnit) -> Unit,
    onToleranceChanged: (Int) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onThemeChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações do WidVelo", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "UNIDADES E TOLERÂNCIA",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unidade de Velocidade", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
                                Text("Exibição no velocímetro e widget", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = currentUnit == SpeedUnit.KMH,
                                    onClick = { onUnitChanged(SpeedUnit.KMH) },
                                    label = { Text("km/h") }
                                )
                                FilterChip(
                                    selected = currentUnit == SpeedUnit.MPH,
                                    onClick = { onUnitChanged(SpeedUnit.MPH) },
                                    label = { Text("mph") }
                                )
                            }
                        }

                        Divider()

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tolerância de Excesso:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("+ $toleranceKmh km/h", fontWeight = FontWeight.Bold, color = ElectricBlue)
                            }
                            Text(
                                "Evita alertas constantes por pequenas variações do GPS.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(0, 3, 5, 10).forEach { tol ->
                                    FilterChip(
                                        selected = toleranceKmh == tol,
                                        onClick = { onToleranceChanged(tol) },
                                        label = { Text("+$tol km/h") }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ALERTAS E APARÊNCIA",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Alertas Sonoros (Beep)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
                                Text("Beep ao se aproximar de radares e excesso", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = onSoundChanged
                            )
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Modo Escuro (Noturno)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
                                Text("Fundo preto OLED para dirigir à noite", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = darkThemeEnabled,
                                onCheckedChange = onThemeChanged
                            )
                        }
                    }
                }
            }
        }
    }
}
