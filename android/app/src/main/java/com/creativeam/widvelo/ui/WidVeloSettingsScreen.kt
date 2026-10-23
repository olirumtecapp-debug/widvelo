package com.creativeam.widvelo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeam.widvelo.BuildConfig
import com.creativeam.widvelo.location.SpeedUnit
import com.creativeam.widvelo.ui.theme.AlertRed
import com.creativeam.widvelo.ui.theme.CautionAmber
import com.creativeam.widvelo.ui.theme.CyanAccent
import com.creativeam.widvelo.ui.theme.ElectricBlue
import com.creativeam.widvelo.ui.theme.SafeGreen

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
    onCheckForUpdates: () -> Unit = {},
    isSimulationRunning: Boolean = false,
    onToggleSimulation: () -> Unit = {},
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configurações do WidVelo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "UNIDADES E TOLERÂNCIA",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                    maxLines = 1
                )
            }

            // Card 1: Unidade de Velocidade & Tolerância
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Linha 1: Seletor Segmentado de Unidade (km/h vs mph)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Unidade de Velocidade",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Exibição no velocímetro e widget",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Segmented Controls km/h | mph
                            Row(
                                modifier = Modifier.width(136.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    SpeedUnit.KMH to "km/h",
                                    SpeedUnit.MPH to "mph"
                                ).forEach { (unit, label) ->
                                    val isSelected = currentUnit == unit
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) ElectricBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onUnitChanged(unit) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Linha 2: Tolerância de Excesso com 4 Colunas Justificadas com weight(1f)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tolerância de Excesso:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "+ $toleranceKmh km/h",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ElectricBlue,
                                    maxLines = 1
                                )
                            }

                            Text(
                                text = "Evita alertas sonoros por pequenas variações do GPS.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // 4 Botões Segmentados Perfeitamente Alinhados e Justificados
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(0, 3, 5, 10).forEach { tol ->
                                    val isSelected = toleranceKmh == tol
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) ElectricBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onToleranceChanged(tol) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$tol km/h",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 2: Atualizações e Versão (OTA)
            item {
                Text(
                    text = "ATUALIZAÇÕES E VERSÃO",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                    maxLines = 1
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Versão Instalada",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) • OTA Ativo",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Button(
                            onClick = onCheckForUpdates,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verificar Atualizações do App",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Card 3: Alertas e Aparência
            item {
                Text(
                    text = "ALERTAS E APARÊNCIA",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                    maxLines = 1
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Alertas Sonoros (Beep)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Beep ao se aproximar de radares e excesso",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = onSoundChanged
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Modo Escuro (Noturno)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Fundo preto OLED para dirigir à noite",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = darkThemeEnabled,
                                onCheckedChange = onThemeChanged
                            )
                        }
                    }
                }
            }

            // Card 3.5: Como Funcionam os Radares e Semáforos + Teste de Alertas
            item {
                Text(
                    text = "RADAR, FAROL E DEMONSTRAÇÃO",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                    maxLines = 1
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CyanAccent)
                            Text(
                                text = "Como os Alertas Aparecem?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Text(
                            text = """• Radares Homologados: Monitorados em um cone direcional de 800m à frente no mesmo sentido da via (ex: Rua dos Vianas e Av. Ítalo Setti). A distância diminui em tempo real e emite aviso sonoro ao se aproximar.

• Semáforos com Temporizador: Avisam cruzamentos a até 300m. Se o veículo parar (0 km/h), o app e o widget exibem o tempo estimado restante do ciclo do farol.

• Widget Flutuante Estilo Waze: Em velocidade normal, fica no design escuro clean sem distrações. Ao ultrapassar o limite, fica VERMELHO e exibe a placa de velocidade ao lado.""",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 17.sp
                        )

                        Button(
                            onClick = onToggleSimulation,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSimulationRunning) AlertRed else ElectricBlue
                            )
                        ) {
                            Text(
                                text = if (isSimulationRunning) "⏹ Parar Simulação de Alertas" else "🧪 Testar Alertas & Simular Rota SBC",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Card 4: Sobre o Aplicativo
            item {
                Text(
                    text = "SOBRE O APLICATIVO",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                    maxLines = 1
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "WidVelo",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Velocímetro GPS, Radares & Widget Flutuante",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Versão do App:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1)
                            Text("v${BuildConfig.VERSION_NAME}", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Código da Build:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1)
                            Text("${BuildConfig.VERSION_CODE}", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sistema Operacional:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1)
                            Text("Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})", fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dispositivo:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1)
                            Text("${android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${android.os.Build.MODEL}", fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Banco de Dados:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1)
                            Text("Room SQLite v4 (SBC)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SafeGreen, maxLines = 1)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Canal de Entrega:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1)
                            Text("OTA Direto GitHub", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = CyanAccent, maxLines = 1)
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text(
                            text = "© 2026 CreativeAM / OlirumTec • Todos os direitos reservados.\nDesenvolvido com foco em precisão, leveza e condução segura.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
