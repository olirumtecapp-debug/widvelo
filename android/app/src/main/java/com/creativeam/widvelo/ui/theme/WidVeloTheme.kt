package com.creativeam.widvelo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ElectricBlue = Color(0xFF0284C7)
val DeepSkyBlue = Color(0xFF0066FF)
val CyanAccent = Color(0xFF06B6D4)
val Graphite = Color(0xFF0F172A)
val SlateGray = Color(0xFF64748B)
val LightBg = Color(0xFFF8FAFC)
val DarkBg = Color(0xFF0A0B0E)
val DarkSurface = Color(0xFF1E293B)
val AlertRed = Color(0xFFEF4444)
val CautionAmber = Color(0xFFF59E0B)
val SafeGreen = Color(0xFF10B981)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    secondary = CyanAccent,
    background = LightBg,
    surface = Color.White,
    onSurface = Graphite,
    onBackground = Graphite
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    secondary = CyanAccent,
    background = DarkBg,
    surface = DarkSurface,
    onSurface = Color.White,
    onBackground = Color.White
)

@Composable
fun WidVeloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
