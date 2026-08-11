package com.example.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Blue = Color(0xFF4F8CFF)
private val Green = Color(0xFF22C55E)

private val DarkColors = darkColorScheme(
    primary = Blue,
    secondary = Green,
)
private val LightColors = lightColorScheme(
    primary = Blue,
    secondary = Green,
)

@Composable
fun TrackerTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
